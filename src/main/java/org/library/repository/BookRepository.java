package org.library.repository;

import lombok.NonNull;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookRepository implements BookRepositoryInterface {

    @Override
    public Book save(@NonNull Book book) {
        String query = "INSERT INTO books (title, author, year, publisher, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setInt(3, book.getYear());
            statement.setString(4, book.getPublisher());
            statement.setString(5, book.getStatus().name());

            int rows = statement.executeUpdate();
            if (rows == 0) throw new RuntimeException("Faild to save book");

            try (ResultSet keys = statement.executeQuery()) {
                if (keys.next()) {
                    book.setBookID(keys.getLong(1));
                }
            }

            return book;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save book", e);
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        String query = "SELECT * FROM books WHERE book_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToBook(resultSet));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find book by id", e);
        }
    }

    @Override
    public List<Book> findAll() {
        String query = "SELECT * FROM books";
        List<Book> books = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                books.add(mapResultSetToBook(resultSet));
            }

            return books;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all books", e);
        }
    }

    @Override
    public List<Book> findAvailable() {
        String query = "SELECT * FROM books WHERE status = 'available'";
        List<Book> books = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                books.add(mapResultSetToBook(resultSet));
            }

            return books;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find available books", e);
        }
    }

    @Override
    public List<Book> findByTitle(String title) {
        String query = "SELECT * FROM books WHERE title = ?";
        List<Book> books = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapResultSetToBook(resultSet));
                }
            }

            return books;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find books by title", e);
        }
    }

    @Override
    public Boolean update(Book book) {
        String query = """
                UPDATE books
                SET title = ?, author = ?, year = ?, publisher = ?, status = ?
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setInt(3, book.getYear());
            statement.setString(4, book.getPublisher());
            statement.setString(5, book.getStatus().name());
            statement.setLong(6, book.getBookID());

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update book", e);
        }
    }

    @Override
    public Boolean updateStatus(Long id, BookStatus status) {
        String query = "UPDATE books SET status = ? WHERE book_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setString(1, status.name());
            statement.setLong(2, id);
            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update book status", e);
        }
    }

    @Override
    public Boolean delete(Long id) {
        String query = "DELETE FROM books WHERE book_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)
        ) {

            statement.setLong(1, id);
            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete book", e);
        }
    }

    private Book mapResultSetToBook(ResultSet resultSet) throws SQLException {
        return new Book(
                resultSet.getLong("book_id"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getInt("year"),
                resultSet.getString("publisher"),
                BookStatus.valueOf(resultSet.getString("status"))
        );
    }
}