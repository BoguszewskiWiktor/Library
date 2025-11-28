package org.library.repository;

import org.library.model.Loan;
import org.library.model.LoanStatus;
import org.library.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanRepository implements LoanRepositoryInterface {
    @Override
    public Loan save(Loan loan) {
        String query = "INSERT INTO loan " +
                "(user_id, book_id, loan_date, due_date, return_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "RETURNING id";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, loan.getUserId());
            statement.setLong(2, loan.getBookId());
            statement.setObject(3, loan.getLoanDate());
            statement.setObject(4, loan.getDueDate());
            statement.setObject(5, loan.getReturnDate());
            statement.setString(6, loan.getStatus().name());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                loan.setLoanId(resultSet.getLong("id"));
            }

            return loan;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save loan", e);
        }
    }

    @Override
    public Optional<Loan> findById(Long id) {
        String query = "SELECT * FROM loan WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToLoan(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find loan by id", e);
        }
    }

    @Override
    public List<Loan> findByUserId(Long userId) {
        String query = "SELECT * FROM loan WHERE user_id = ?";
        List<Loan> loans = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    loans.add(mapResultSetToLoan(resultSet));
                }

                return loans;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find loan by user id", e);
        }
    }

    @Override
    public List<Loan> findActiveByUserId(Long userId) {
        String query = "SELECT * FROM loan WHERE user_id = ? AND status = 'ACTIVE'";
        List<Loan> loans = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    loans.add(mapResultSetToLoan(resultSet));
                }

                return loans;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find loan active by user id", e);
        }
    }

    @Override
    public List<Loan> findByBookId(Long bookId) {
        String query = "SELECT * FROM loan WHERE book_id = ?";
        List<Loan> loans = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    loans.add(mapResultSetToLoan(resultSet));
                }

                return loans;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find loan by book id", e);
        }

    }

    @Override
    public List<Loan> findActiveByBookId(Long bookId) {
        String query = "SELECT * FROM loan WHERE book_id = ? AND status = 'ACTIVE'";
        List<Loan> loans = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    loans.add(mapResultSetToLoan(resultSet));
                }

                return loans;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find loan active by book id", e);
        }
    }

    @Override
    public int countActiveByUserId(Long userId) {
        String query = "SELECT COUNT(*) AS total FROM loan WHERE user_id = ? AND status = 'ACTIVE'";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count loan active by user id", e);
        }
    }

    @Override
    public Boolean markReturned(Long loanId, LocalDate returnDate) {
        String query = """
                UPDATE loan
                SET return_date = ?, status = 'RETURNED'
                WHERE id = ? AND status = 'ACTIVE'
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setObject(1, returnDate);
            statement.setLong(2, loanId);

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark loan returned", e);
        }
    }

    @Override
    public Optional<Loan> findActiveByUserIdAndBookId(Long userId, Long bookId) {
        String query = "SELECT * FROM loan WHERE user_id = ? AND book_id = ? AND status = 'ACTIVE' LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, userId);
            statement.setLong(2, bookId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToLoan(resultSet));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find loan active by user id and book id", e);
        }
    }

    @Override
    public Boolean deleteById(Long id) {
        String query = "DELETE FROM loan WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);
            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete loan by id", e);
        }
    }

    private Loan mapResultSetToLoan(ResultSet resultSet) throws SQLException {
        Loan loan = new Loan();

        loan.setLoanId(resultSet.getLong("id"));
        loan.setUserId(resultSet.getLong("user_id"));
        loan.setBookId(resultSet.getLong("book_id"));

        try {
            LocalDate loanDate = resultSet.getObject("loan_date", LocalDate.class);
            LocalDate dueDate = resultSet.getObject("due_date", LocalDate.class);
            LocalDate returnDate = resultSet.getObject("return_date", LocalDate.class);

            loan.setLoanDate(loanDate);
            loan.setDueDate(dueDate);
            loan.setReturnDate(returnDate);
        } catch (AbstractMethodError | SQLException e) {
            java.sql.Date loanDate = resultSet.getDate("loan_date");
            loan.setLoanDate(loanDate == null ? null : loanDate.toLocalDate());

            java.sql.Date dueDate = resultSet.getDate("due_date");
            loan.setDueDate(dueDate == null ? null : dueDate.toLocalDate());

            java.sql.Date returnDate = resultSet.getDate("return_date");
            loan.setReturnDate(returnDate == null ? null : returnDate.toLocalDate());
        }

        String status = resultSet.getString("status");
        loan.setStatus(status == null ? null : LoanStatus.valueOf(status));

        return loan;
    }
}