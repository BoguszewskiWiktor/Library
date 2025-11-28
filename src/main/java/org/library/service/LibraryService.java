package org.library.service;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.library.model.Book;
import org.library.model.Loan;
import org.library.model.User;
import org.library.repository.BookRepository;
import org.library.util.Result;

import java.util.List;
import java.util.Optional;

@Slf4j
@Data
public class LibraryService {
    private final BookService bookService;
    private final LoanService loanService;
    private final BookRepository bookRepository;

    public Result borrowBook(User user, Book book) {
        return loanService.borrowBook(user.getUserId(), book.getBookID());
    }

    public Result returnBook(User user, Book book) {
        return loanService.returnBook(user.getUserId(), book.getBookID());
    }

    public List<Book> getUserBorrowedBooks(@NonNull Long userId) {
        log.debug("Fetching borrowed books for user {}", userId);

        List<Loan> activeLoans = loanService.getActiveLoansForUser(userId);
        if (activeLoans.isEmpty()) {
            log.info("No borrowed books for user {}", userId);
        }

        return activeLoans.stream()
                .map(loan -> {
                    Optional<Book> book = bookRepository.findById(loan.getBookId());
                    if (book.isEmpty()) {
                        log.warn("Book not found for bookId: {}", loan.getBookId());
                    }
                    return book;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public Optional<Book> getBookById(@NonNull Long bookId) {
        return bookRepository.findById(bookId);
    }

    public List<Book> searchBookByTitle(@NonNull String title) {
        return bookService.searchBookByTitle(title);
    }

    public List<Book> getBooks() {
        return bookService.listAllBooks();
    }
}