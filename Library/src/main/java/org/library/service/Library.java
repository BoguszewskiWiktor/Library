package org.library.service;

import lombok.Data;
import lombok.NonNull;
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


@Data
public class Library {
    private final UserManager userManager;
    private final BookManager bookManager;

    public Result borrowBook(User user, Book book) {
        Map<String, Object> values = new HashMap<>();
        values.put("user", user);
        values.put("book", book);

        return safeCall(
                () -> {
                    ValidateUtils.requireNonNull(values);

//                    Sprawdzenie, czy użytkownik może wypożyczyć książkę
                    if (!userManager.canBorrowBook(user)) {
                        return Result.failure("User " + user.getEmail() + " is not allowed to borrow book.");
                    }

//                    Sprawdzenie, czy książka jest możliwa do wypożyczenia
                    if (!bookManager.isBookAvailable(book)) {
                        return Result.failure("Book " + book.getTitle() + " cannot be borrowed.");
                    }

                    return safeRun(
                            () -> {
                                book.setStatus(BookStatus.BORROWED);
                                user.getBorrowedBooks().add(book);
                            },
                            "Failed to borrow book " + book.getTitle(),
                            () -> Result.success(
                                    "Book " + book.getTitle() + " is borrowed successfully by " + user.getEmail())
                    );
                }, Result.failure("Unexpected error while borrowing book."),
                "Library.borrowBook");
    }

    public Result returnBook(User user, Book book) {
        Map<String, Object> values = new HashMap<>();
        values.put("user", user);
        values.put("book", book);

        return safeCall(() -> {
                    ValidateUtils.requireNonNull(values);

//                    Sprawdzenie, czy użytkownik może zwrócić książkę
                    if (!userManager.canReturnBook(user, book)) {
                        return Result.failure
                                ("User " + user.getEmail() + " cannot return book " + book.getTitle() + ".");
                    }

//                    Sprawdzenie, czy książka jest poprawna
                    if (!bookManager.isBookCorrect(book)) {
                        return Result.failure("Book is not found in system.");
                    }

                    return safeRun(
                            () -> {
                                book.setStatus(BookStatus.AVAILABLE);
                                user.getBorrowedBooks().remove(book);
                            },
                            "Failed to return book " + book.getTitle(),
                            () -> Result.success(
                                    "Book " + book.getTitle() + " is returned successfully by " + user.getEmail())
                    );
                }, Result.failure("Unexpected error while returning book."),
                "Library.returnBook");
    }

    public List<Book> getUserBorrowedBooks(@NonNull User user) {
        return user.getBorrowedBooks();
    }

    public List<Book> getBooks() {
        return bookManager.getBooks();
    }
}