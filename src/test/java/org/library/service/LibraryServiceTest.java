package org.library.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.model.User;
import org.library.util.Result;

import java.util.List;

public class LibraryServiceTest {

    private BookService bookService;
    private UserService userService;
    private LibraryService libraryService;

    @BeforeEach
    void setup()
    {
        bookService = new BookService();
        userService = new UserService();
        libraryService = new LibraryService(userService, bookService);
    }

    @Test
    void borrowBook_shouldFailWhenUserIsNull() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();

        // when
        Result result = libraryService.borrowBook(null, book);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("User cannot be null", result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void borrowBook_shouldFailWhenBookIsNull() {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();

        // when
        Result result = libraryService.borrowBook(user, null);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("Book cannot be null", result.getMessage());
        Assertions.assertEquals(0, user.getBorrowedBooks().size());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void borrowBook_shouldFailWhenUserCannotBorrowBook() {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();

        // when
        Result result = libraryService.borrowBook(user, book);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(
                "User " + user.getEmail() + " is not allowed to borrow book.", result.getMessage());
        Assertions.assertEquals(0, user.getBorrowedBooks().size());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void borrowBook_shouldFailWhenBookIsNotAvailable() {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();
        user.setLoggedIn(true);
        book.setStatus(BookStatus.BORROWED);

        // when
        Result result = libraryService.borrowBook(user, book);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("Book " + book.getTitle() + " cannot be borrowed.", result.getMessage());
        Assertions.assertEquals(0, user.getBorrowedBooks().size());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void borrowBook_shouldSuccessfullyBorrowBook() {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();
        user.setLoggedIn(true);

        // when
        Result result = libraryService.borrowBook(user, book);

        // then
        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals("BORROWED", book.getStatus().name());
        Assertions.assertEquals(1, user.getBorrowedBooks().size());
        Assertions.assertEquals(
                "Book " + book.getTitle() + " is borrowed successfully by " + user.getEmail(),
                result.getMessage()
        );
    }

    @Test
    void returnBook_shouldFailWhenUserIsNull() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();

        // when
        Result result = libraryService.returnBook(null, book);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("User cannot be null", result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void returnBook_shouldFailWhenBookIsNull() {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        int size = user.getBorrowedBooks().size();

        // when
        Result result = libraryService.returnBook(user, null);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("Book cannot be null", result.getMessage());
        Assertions.assertEquals(0, size);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void returnBook_ShouldFailWhenUserCannotReturnBook() {
        // when
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();
        int size = user.getBorrowedBooks().size();

        // when
        Result result = libraryService.returnBook(user, book);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("User " + user.getEmail() + " cannot return book " + book.getTitle() + ".",
                result.getMessage());
        Assertions.assertEquals(0, size);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void returnBook_shouldFailWhenBookIsNotInTheSystem() {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        user.setLoggedIn(true);
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        int size = user.getBorrowedBooks().size();

        // when
        Result result = libraryService.returnBook(user, book);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("Book is not found in system.", result.getMessage());
        Assertions.assertEquals(0, size);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void returnBook_shouldSuccessfullyReturnBook() {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();
        user.setLoggedIn(true);
        user.getBorrowedBooks().add(book);

        // when
        int borrowedBooksListSize = user.getBorrowedBooks().size();
        Result result = libraryService.returnBook(user, book);

        // then
        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals(1, borrowedBooksListSize);
        Assertions.assertEquals("Book " + book.getTitle() + " is returned successfully by " + user.getEmail(),
                result.getMessage());
        Assertions.assertEquals(0, user.getBorrowedBooks().size());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void getUserBorrowedBooks_shouldCorrectlyReturnBooksBorrowedByUser () {
        // given
        userService.registerUser("andrzej.kowalski@gmail.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@gmail.com").get();
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        Book book1 = bookService.getBooks().getFirst();
        Book book2 = bookService.getBooks().getLast();
        user.setLoggedIn(true);
        int size = user.getBorrowedBooks().size();
        user.getBorrowedBooks().add(book1);
        user.getBorrowedBooks().add(book2);

        // when
        List<Book> userBorrowedBooks = libraryService.getUserBorrowedBooks(user);

        // then
        Assertions.assertEquals(0, size);
        Assertions.assertEquals(book1.getTitle(), userBorrowedBooks.get(0).getTitle());
        Assertions.assertEquals(book2.getTitle(), userBorrowedBooks.get(1).getTitle());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void getUserBorrowedBooks_shouldThrowNullPointerExceptionWhenUserIsNull() {
        // given, when, then
        Assertions.assertThrows(
                NullPointerException.class, () -> libraryService.getUserBorrowedBooks(null));
    }
}