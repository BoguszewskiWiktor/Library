package org.library.service;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.model.User;
import org.library.util.Result;

import java.util.List;

@Slf4j
@Data
public class LibraryService {
    private final UserService userService;
    private final BookService bookService;

    public Result borrowBook(User user, Book book) {
        log.info("Borrow request");

        Result validation = validateBorrowInputs(user, book);
        if (validation != null) return validation;

        if (!bookService.isBookAvailable(book)) {
            log.warn("Book {} is not available for borrowing", book.getTitle());
            return Result.failure("Book " + book.getTitle() + " cannot be borrowed.");
        }

        if (!userService.canBorrowBook(user)) {
            log.warn("User {} is not allowed to borrow book '{}'", user.getEmail(), book.getTitle());
            return Result.failure("User " + user.getEmail() + " is not allowed to borrow book.");
        }

        book.borrow();
        user.getBorrowedBooks().add(book);

        log.info("Book {} borrowed successfully by {}", book.getTitle(), user.getEmail());
        return Result.success("Book " + book.getTitle() + " is borrowed successfully by " + user.getEmail());
    }

    public Result returnBook(User user, Book book) {
        log.info("Return request");

        Result validation = validateBorrowInputs(user, book);
        if (validation != null) return validation;

        if (!bookService.isBookCorrect(book)) {
            log.error("Book {} not found in system during return attempt", book.getTitle());
            return Result.failure("Book is not found in system.");
        }

        if (!userService.canReturnBook(user, book)) {
            log.warn("User {} attempted to return a book they cannot: {}", user.getEmail(), book.getTitle());
            return Result.failure("User " + user.getEmail() + " cannot return book " + book.getTitle() + ".");
        }

        book.setStatus(BookStatus.AVAILABLE);
        user.returnBook(book);

        log.info("Book {} returned successfully by {}", book.getTitle(), user.getEmail());
        return Result.success("Book " + book.getTitle() + " is returned successfully by " + user.getEmail());
    }

    public List<Book> getUserBorrowedBooks(@NonNull User user) {
        log.debug("Fetching borrowed books for user {}", user.getEmail());
        return user.getBorrowedBooks();
    }

    public List<Book> getBooks() {
        log.debug("Fetching all books from the library");
        return bookService.getBooks();
    }

    private Result validateBorrowInputs(User user, Book book) {
        return user == null ? Result.failure("User cannot be null")
                : book == null ? Result.failure("Book cannot be null")
                : null;
    }
}