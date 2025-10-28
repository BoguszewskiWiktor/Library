package org.library.service;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.model.User;
import org.library.util.Result;
import org.library.util.ValidateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.library.util.ExceptionHandler.safeCall;
import static org.library.util.ExceptionHandler.safeRun;

@Slf4j
@Data
public class LibraryService {
    private final UserService userService;
    private final BookService bookService;

    public Result borrowBook(User user, Book book) {
        log.info("Borrow request");

        Map<String, Object> values = new HashMap<>();
        values.put("user", user);
        values.put("book", book);

        return safeCall(
                () -> {
                    ValidateUtils.requireNonNull(values);

//                    Sprawdzenie, czy użytkownik może wypożyczyć książkę
                    if (!userService.canBorrowBook(user)) {
                        log.warn("User {} is not allowed to borrow book '{}'", user.getEmail(), book.getTitle());
                        return Result.failure("User " + user.getEmail() + " is not allowed to borrow book.");
                    }

//                    Sprawdzenie, czy książka jest możliwa do wypożyczenia
                    if (!bookService.isBookAvailable(book)) {
                        log.warn("Book {} is not available for borrowing", book.getTitle());
                        return Result.failure("Book " + book.getTitle() + " cannot be borrowed.");
                    }

                    return safeRun(
                            () -> {
                                book.setStatus(BookStatus.BORROWED);
                                user.getBorrowedBooks().add(book);
                                log.info("Book {} borrowed successfully by {}", book.getTitle(), user.getEmail());
                            },
                            "Failed to borrow book " + book.getTitle(),
                            () -> Result.success(
                                    "Book " + book.getTitle() + " is borrowed successfully by " + user.getEmail())
                    );
                }, Result.failure("Unexpected error while borrowing book."),
                "Library.borrowBook");
    }

    public Result returnBook(User user, Book book) {
        log.info("Return request");

        Map<String, Object> values = new HashMap<>();
        values.put("user", user);
        values.put("book", book);

        return safeCall(() -> {
                    ValidateUtils.requireNonNull(values);

//                    Sprawdzenie, czy użytkownik może zwrócić książkę
                    if (!userService.canReturnBook(user, book)) {
                        log.warn("User {} attemted to return a book they cannot:  {}", user.getEmail(), book.getTitle());
                        return Result.failure
                                ("User " + user.getEmail() + " cannot return book " + book.getTitle() + ".");
                    }

//                    Sprawdzenie, czy książka jest poprawna
                    if (!bookService.isBookCorrect(book)) {
                        log.error("Book {} not found in system during return attempt", book.getTitle());
                        return Result.failure("Book is not found in system.");
                    }

                    return safeRun(
                            () -> {
                                book.setStatus(BookStatus.AVAILABLE);
                                user.getBorrowedBooks().remove(book);
                                log.info("Book {} returned successfully by {}", book.getTitle(), user.getEmail());
                            },
                            "Failed to return book " + book.getTitle(),
                            () -> Result.success(
                                    "Book " + book.getTitle() + " is returned successfully by " + user.getEmail())
                    );
                }, Result.failure("Unexpected error while returning book."),
                "Library.returnBook");
    }

    public List<Book> getUserBorrowedBooks(@NonNull User user) {
        log.debug("Fetching borrowed books for user '{}'", user.getEmail());
        return user.getBorrowedBooks();
    }

    public List<Book> getBooks() {
        log.debug("Fetching all books from the library");
        return bookService.getBooks();
    }
}