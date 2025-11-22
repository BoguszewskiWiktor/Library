package org.library.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.library.model.Book;
import org.library.model.User;
import org.library.util.Result;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final int MAX_BORROW_LIMIT = 5;

    public Result registerUser(@NonNull String email, @NonNull String fullName, @NonNull String password) {
        String normalizedEmail = normalizeEmail(email);

        log.info("Attempting to register user: {} ({})", fullName, normalizedEmail);

        Result registrationInput = validateRegistrationInputs(normalizedEmail, fullName, password);
        if (registrationInput != null) return registrationInput;

        Result emailFormat = isEmailFormatValid(normalizedEmail);
        if (emailFormat != null) return emailFormat;

        Result fullNameValid = isFullNameValid(normalizedEmail, fullName);
        if (fullNameValid != null) return fullNameValid;

        Result strongPassword = isPasswordStrongEnough(normalizedEmail, password);
        if (strongPassword != null) return strongPassword;

        boolean userExists = users.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(normalizedEmail));

        if (userExists) {
            log.warn("User with email {} already exists.", normalizedEmail);
            return Result.failure("User with email address " + normalizedEmail + " already exists.");
        }

        String newId = generatedUserId();
        String hashedPassword = hashPassword(password);
        User newUser = new User(newId, fullName, normalizedEmail, hashedPassword);
        users.add(newUser);

        log.debug("Created new user: id={}, fullName={}, email={}", newId, fullName, normalizedEmail);

        log.info("User {} successfully registered.", fullName);
        return Result.success("User " + newUser.getEmail() + " has been successfully registered.");
    }

    public Result loginUser(@NonNull String email, @NonNull String password) {
        String normalizedEmail = normalizeEmail(email);

        log.info("Attempting to login user: {}", password);

        Optional<User> optionalUser = getUserByEmail(normalizedEmail);
        if (optionalUser.isEmpty()) {
            log.error("Account with email {} does not exist.", normalizedEmail);
            return Result.failure("User with email " + email + " does not exist.");
        }

        User user = optionalUser.get();
        String hashedPassword = hashPassword(password);

        if (!user.getPassword().equals(hashedPassword)) {
            log.warn("Invalid password.");
            return Result.failure("Invalid password.");
        } else if (user.isLoggedIn()) {
            log.warn("User {} is already logged in.", normalizedEmail);
            return Result.failure(user.getFullName() + " is already logged in.");
        } else {
            user.setLoggedIn(true);
            log.info("User {} successfully logged in.", normalizedEmail);
            return Result.success(user.getFullName() + " successfully logged in.");
        }
    }

    public Result logoutUser(@NonNull User user) {
        log.info("Attempting to logout user: {}", user.getEmail());

        if (!user.isLoggedIn()) {
            log.warn("User {} is not logged in.", user.getEmail());
            return Result.failure(user.getFullName() + " is not logged in.");
        }

        user.setLoggedIn(false);
        log.info("User {} successfully logged out.", user.getEmail());
        return Result.success(user.getFullName() + " has been logged out successfully.");
    }

    public Boolean canBorrowBook(@NonNull User user) {
        log.info("Checking if user {} can borrow book.",  user.getEmail());

        if (!isUserCorrect(user)) {
            log.error("Account with email {} does not exist.", user.getEmail());
            return false;
        }

        if (!user.isLoggedIn()) {
            log.warn("User {} is not logged in.", user.getEmail());
            return false;
        }

        if (user.getBorrowedBooks().size() >= MAX_BORROW_LIMIT) {
            log.warn("User {} has too many borrowed books.", user.getEmail());
            return false;
        }

        log.info("User {} can borrow book", user.getEmail());
        return true;
    }

    public Boolean canReturnBook(@NonNull User user, @NonNull Book book) {
        log.info("Checking if user {} can return book.",  user.getEmail());

        if (!isUserCorrect(user)) {
            log.error("Account with email {} does not exist.", user.getEmail());
            return false;
        }

        if (!user.isLoggedIn()) {
            log.warn("User {} is not logged in.", user.getEmail());
            return false;
        }

        if (!user.getBorrowedBooks().contains(book)) {
            log.warn("User {} has not borrowed book {}.", user.getEmail(), book.getTitle());
            return false;
        }

        log.info("User {} can return book {}.", user.getEmail(), book.getTitle());
        return true;
    }

    public String hashPassword(@NonNull String password) {
        log.info("Attempting to hash password");
        log.debug("Hashing password");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            log.info("Password hashing successful");
            log.debug("Password after hashing: {}", hexString);
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error occurred during password hashing.", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private @NonNull String generatedUserId() {
        String id = UUID.randomUUID().toString();
        log.info("Generated new user ID: {}", id);
        return id;
    }

    public Optional<User> getUserByEmail(@NonNull String email) {
        log.info("Getting user with email: {}", normalizeEmail(email));

        Optional<User> user = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(normalizeEmail(email)))
                .findFirst();
        if (user.isPresent()) {
            log.info("User found: {}", user.get().getEmail());
        } else {
            log.warn("User with email {} not found.", normalizeEmail(email));
        }
        return user;
    }

    public Boolean isUserCorrect(@NonNull User user) {
        log.debug("Validating user: {}", user.getEmail());

        boolean correct = getUserByEmail(user.getEmail()).isPresent();
        if (correct) {
            log.info("User {} is valid.", user.getEmail());
        } else {
            log.warn("User {} is invalid.", user.getEmail());
        }
        return correct;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private Result isPasswordStrongEnough(String email, String password) {
        if (password.length() < 8) {
            log.warn("Registration failed for {}. Password too short ({} characters).",
                    email, password.length());
            return Result.failure("Invalid password length. Password should be at least 8 characters.");
        }
        return null;
    }

    private Result isFullNameValid(String email, String fullName) {
        if (!fullName.trim().contains(" ")) {
            log.warn("Registration failed for {}. Invalid full name '{}'.", email, fullName);
            return Result.failure("Invalid full name format. Full name must contain whitespace between name and surname.");
        }
        return null;
    }

    private Result isEmailFormatValid(String email) {
        if (!email.contains("@")) {
            log.warn("Registration failed for {}. Invalid email format.", email);
            return Result.failure("Invalid email format. Email address must contain @.");
        }
        return null;
    }

    private Result validateRegistrationInputs(String email, String fullName, String password) {
        if (email.isBlank() || fullName.isBlank() || password.isBlank()) {
            log.warn("User validation failed - one or more required fields are empty " +
                    "(email: {}, fullName: {}, password: {}) ", email, fullName, password);
            return Result.failure("User email, full name, and password cannot be empty");
        }
        return null;
    }
}