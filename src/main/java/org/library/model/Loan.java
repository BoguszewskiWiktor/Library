package org.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Loan {
    private Long loanId;

    private Long userId;

    private Long bookId;
    private LocalDate loanDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private LoanStatus status;

    public Loan() {
    }

    public Loan(Long userId, Long bookId, LocalDate loanDate, LocalDate returnDate, LocalDate dueDate, LoanStatus status) {
        this.userId = userId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Boolean isActive() {
        return returnDate == null;
    }

    public void markReturned(LocalDate date) {
        returnDate = date;
    }
}