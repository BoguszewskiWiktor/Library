package org.library.repository;

import org.library.model.Loan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepositoryInterface {
    Loan save(Loan loan);

    Optional<Loan> findById(Long id);

    List<Loan> findByUserId(Long userId);

    List<Loan> findActiveByUserId(Long userId);

    List<Loan> findByBookId(Long bookId);

    List<Loan> findActiveByBookId(Long bookId);

    int countActiveByUserId(Long userId);

    Boolean markReturned(Long loanId, LocalDate returnDate);

    Optional<Loan> findActiveByUserIdAndBookId(Long userId, Long bookId);

    Boolean deleteById(Long id);
}
