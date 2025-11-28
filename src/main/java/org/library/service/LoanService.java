package org.library.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.library.model.*;
import org.library.repository.BookRepository;
import org.library.repository.LoanRepository;
import org.library.repository.UserRepository;
import org.library.util.Result;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Data
public class LoanService {

    private UserRepository userRepository;
    private BookRepository bookRepository;
    private LoanRepository loanRepository;

    public Result borrowBook(Long userId, Long bookId) {
        log.info("Borrow request");

        if (userId == null || bookId == null) {
            log.error("Borrow failed. User or book id is null");
            return Result.failure("User or book id is null");
        }

        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isEmpty()) {
            log.error("Borrow failed. Book not found");
            return Result.failure("Book not found");
        }

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("Borrow failed. User not found");
            return Result.failure("User not found");
        }

        if (!loanRepository.findActiveByBookId(bookId).isEmpty()) {
            log.error("Borrow failed. Book is already borrowed");
            return Result.failure("Book is already borrowed");
        }

        if (!user.get().isLoggedIn()) {
            log.error("Borrow failed. User is not logged in");
            return Result.failure("User is not logged in");
        }

        if (canBorrow(userId)) {
            log.error("Borrow failed. User has reached the borrowing limit");
            return Result.failure("User has reached the maximum number of borrowed books: (5)");
        }

        Loan loan = new Loan(
                userId,
                bookId,
                LocalDate.now(),
                null,
                LocalDate.now().plusMonths(1),
                LoanStatus.ACTIVE
        );

        loanRepository.save(loan);

        book.get().setStatus(BookStatus.BORROWED);
        bookRepository.update(book.get());

        log.info("Borrow successful. Loan ID: {}", loan.getLoanId());
        return Result.success("Book borrowed successfully. Loan id: " + loan.getLoanId());
    }

    public Result returnBook(Long userId, Long bookId) {
        log.info("Return request");

        if (userId == null || bookId == null) {
            log.error("Return failed. User or book id is null");
            return Result.failure("User or book id is null");
        }

        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isEmpty()) {
            log.error("Return failed. Book not found");
            return Result.failure("Book not found");
        }

        if (userRepository.findById(userId).isEmpty()) {
            log.error("Return failed. User not found");
            return Result.failure("User not found");
        }

        Optional<Loan> activeLoan = loanRepository.findActiveByUserIdAndBookId(userId, bookId);
        if (activeLoan.isEmpty()) {
            log.error("Return failed. No active loan for this user and book");
            return Result.failure("No active loan for this user and book");
        }

        loanRepository.markReturned(activeLoan.get().getLoanId(), LocalDate.now());

        book.get().setStatus(BookStatus.AVAILABLE);
        bookRepository.update(book.get());

        log.info("Return successful. Loan ID: {}", activeLoan.get().getLoanId());
        return Result.success("Book returned successfully.");
    }

    public Optional<Loan> getActiveLoanForUserAndBook(Long userId, Long bookId) {
        log.debug("Fetching active loan for user {} and book {}", userId, bookId);
        return loanRepository.findActiveByUserIdAndBookId(userId, bookId);
    }

    public List<Loan> getActiveLoansForUser(Long userId) {
        log.debug("Fetching active loan for user {}", userId);
        return loanRepository.findActiveByUserId(userId);
    }

    public int countActiveLoans(Long userId) {
        log.debug("Fetching count of active loan for user {}", userId);
        return loanRepository.countActiveByUserId(userId);
    }

    public Optional<Loan> findById(Long loanId) {
        log.debug("Fetching loan ID {}", loanId);
        return loanRepository.findById(loanId);
    }

    public Boolean canBorrow(Long userId) {
        return loanRepository.countActiveByUserId(userId) < 6;
    }
}