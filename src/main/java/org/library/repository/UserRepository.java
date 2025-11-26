package org.library.repository;

import org.library.model.User;
import org.library.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository implements UserRepositoryInterface {
    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (fullName, email, password) VALUES (?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());

            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Saving user failed, no rows affected.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }

            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String query = "SELECT * FROM users WHERE user_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapResultSetToUser(resultSet));
            }
            return Optional.empty();


        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {

            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapResultSetToUser(resultSet));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        String query = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    @Override
    public Boolean delete(Integer id) {
        String query = "DELETE FROM users WHERE user_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setInt(1, id);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete book", e);
        }
    }

    @Override
    public Boolean update(User user) {
        String query = """
                UPDATE users
                SET fullName = ?, email = ?, password = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setInt(4, user.getUserId());

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("fullName"),
                resultSet.getString("email"),
                resultSet.getString("password")
        );
    }
}