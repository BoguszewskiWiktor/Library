package org.library.service;

import lombok.NonNull;
import org.library.util.ValidateUtils;
import org.library.model.Result;
import org.library.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static java.util.Map.entry;

public class UserManager {

    private final List<User> users = new ArrayList<>();

    public Result registerUser(@NonNull String email, @NonNull String fullName, @NonNull String password) {
        ValidateUtils.requireNonNull(Map.ofEntries(
                entry("email", email),
                entry("full name", fullName),
                entry("password", password)
        ));
        if (!email.contains("@")) {
            return Result.failure("Invalid email format. Email address must contain @.");
        }

        if (!fullName.contains(" ")) {
            return Result.failure("Invalid full name format. Full name must contain whitespace between name and surname.");
        }

        if (password.length() < 8) {
            return Result.failure("Invalid password length. Password should be at least 8 characters.");
        }

        boolean userExists = users.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (userExists) {
            return Result.failure("User with email address " + email + " already exists.");
        }

        @NonNull String newId = generatedUserId();
        String hashedPassword = hashPassword(password);
        User newUser = new User(newId, fullName, email, hashedPassword);
        users.add(newUser);
        return Result.success("User " + newUser.getEmail() + " has been successfully registered.");
    }

    public Result loginUser(@NonNull String email, @NonNull String password) {
        Optional<User> optionalUser = getUserByEmail(email);
        if (optionalUser.isEmpty()) {
            return Result.failure("User with email " + email + " does not exist.");
        }

        User user = optionalUser.get();
        String hashedPassword = hashPassword(password);

        if (!user.getPassword().equals(hashedPassword)) {
            return Result.failure("Invalid password.");
        } else if (user.isLoggedIn()) {
            return Result.failure(user.getFullName() + " is already logged in.");
        } else {
            user.setLoggedIn(true);
            return Result.success(user.getFullName() + " successfully logged in.");
        }
    }

    public Result logoutUser(@NonNull User user) {
        if (!user.isLoggedIn()) {
            return Result.failure(user.getFullName() + " is not logged in.");
        }

        user.setLoggedIn(false);
        return Result.success(user.getFullName() + " has been logged out successfully.");
    }

    public String hashPassword(@NonNull String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private @NonNull String generatedUserId() {
        return UUID.randomUUID().toString();
    }

    public Optional<User> getUserByEmail(@NonNull String email) {
        return  users.stream()
                .filter(u -> u.getEmail().trim().equalsIgnoreCase(email))
                .findFirst();
    }
}