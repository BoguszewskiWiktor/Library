package org.library.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.library.model.User;
import org.library.repository.UserRepository;
import org.library.util.Result;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LoanService loanService;

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

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            log.warn("User with email {} already exists.", normalizedEmail);
            return Result.failure("User with email address " + normalizedEmail + " already exists.");
        }

        User user = new User(fullName, normalizedEmail, hashPassword(password));
        userRepository.save(user);

        log.debug("Created new user: fullName={}, email={}", user.getFullName(), normalizedEmail);

        log.info("User {} successfully registered.", normalizedEmail);
        return Result.success("User " + normalizedEmail + " has been successfully registered.");
    }

    public Result loginUser(@NonNull String email, @NonNull String password) {
        String normalizedEmail = normalizeEmail(email);
        log.info("Attempting to login user: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            log.error("Account with email {} does not exist.", normalizedEmail);
            return Result.failure("User with email " + normalizedEmail + " does not exist.");
        }

        if (!user.getPassword().equals(hashPassword(password))) {
            log.warn("Invalid password for {}.", normalizedEmail);
            return Result.failure("Invalid password.");
        }

        if (user.isLoggedIn()) {
            log.warn("User {} is already logged in.", normalizedEmail);
            return Result.failure(normalizedEmail + " is already logged in.");
        }

        user.logIn();
        log.info("User {} successfully logged in.", normalizedEmail);
        return Result.success(user.getFullName() + " successfully logged in.");
    }

    public Result logoutUser(@NonNull String email) {
        String normalizedEmail = normalizeEmail(email);
        log.info("Attempting to logout user: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (user == null) {
            log.error("Account with email {} does not exist.", normalizedEmail);
            return Result.failure("User with email " + normalizedEmail + " does not exist.");
        }

        if (!user.isLoggedIn()) {
            log.warn("User {} is not logged in.", user.getEmail());
            return Result.failure("Cannot log out – user is not logged in.");
        }

        user.logOut();
        log.info("User {} successfully logged out.", user.getEmail());
        return Result.success("You have been logged out.");
    }

    public Result deleteUser(@NonNull String email) {
        String normalizedEmail = normalizeEmail(email);
        log.info("Attempting to delete user: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (user == null) {
            log.error("User with email {} does not exist.", normalizedEmail);
            return Result.failure("User with email " + normalizedEmail + " does not exist.");
        }

        if (!user.isLoggedIn()) {
            log.warn("User {} is not logged in. Cannot delete account.", user.getEmail());
            return Result.failure(user.getEmail() + " must be logged in to delete the account.");
        }

        int activeLoans = loanService.countActiveLoans(user.getUserId());
        if (activeLoans > 0) {
            log.warn("User {} cannot be deleted: {} books are currently borrowed and must be returned first.",
                    user.getEmail(), activeLoans);
            return Result.failure(
                    "Cannot delete account. You still have " + activeLoans + " borrowed books.");
        }

        user.logOut();
        userRepository.delete(user.getUserId());

        log.info("User {}  (ID {}) successfully deleted.", user.getEmail(), user.getUserId());
        return Result.success("Account for " + user.getFullName() + " has been successfully deleted.");
    }

    private Optional<User> getLoggedInUser(User user) {
        String normalizedEmail = normalizeEmail(user.getEmail());
        log.info("Attempting to get logged in user: {}", normalizedEmail);

        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isEmpty()) {
            log.error("Account with email {} does not exist.", normalizedEmail);
            return Optional.empty();
        }

        if (!existingUser.get().isLoggedIn()) {
            log.warn("User {} is not logged in.", normalizedEmail);
            return Optional.empty();
        }

        log.info("User {} is  logged in.", normalizedEmail);
        return existingUser;
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
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error occurred during password hashing.", e);
            throw new RuntimeException("Error hashing password", e);
        }
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