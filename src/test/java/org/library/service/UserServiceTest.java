package org.library.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.model.Book;
import org.library.model.User;
import org.library.util.Result;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    // registerUser()
    @SuppressWarnings("DataFlowIssue")
    @Test
    void shouldThrowNullPointerExceptionWhenEmailIsNull() {
        // given, when, then
        Assertions.assertThrows(NullPointerException.class,
                () -> userService.registerUser(null, "Andrzej Kowalski", "Password"));
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        // given, when
        Result result = userService.registerUser("     ", "Andrzej Kowalski", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        // given, when
        Result result = userService.registerUser("123", "Andrzej Kowalski", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void shouldThrowNullPointerExceptionWhenNameIsNull() {
        // given, when, then
        Assertions.assertThrows(NullPointerException.class,
                () -> userService.registerUser("randomEmail@email.com", null, "Password"));
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        // given, when
        Result result = userService.registerUser("randomEmail@email.com", "   ",  "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @Test
    void shouldFailWhenFullNameDoesNotContainSpace() {
        // given, when
        Result result = userService.registerUser("andrzej@email.com", "AndrzejKowalski", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @Test
    void shouldFailWhenPasswordTooShort() {
        // given, when
        Result result = userService.registerUser("Andrzej@email.com", "AndrzejKowalski", "Pass");

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");

        // when
        Result result = userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski2", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());

    }

    @Test
    void shouldSuccessWhenGivenValidInput() {
        // given, when
        Result result = userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");

        // then
        Assertions.assertTrue(result.getSuccess());
    }

    // loginUser()
    @Test
    void shouldFailWhenEmailNotFound() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");

        // when
        Result result = userService.loginUser("andrzejkowalski@email.com", "Password");

        Assertions.assertFalse(result.getSuccess());
    }

    @Test
    void shouldFailWhenPasswordIncorrect() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");

        // when
        Result result = userService.loginUser("andrzejkowalski@email.com", "Password1");

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @Test
    void shouldFailWhenUserAlreadyLoggedIn() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");
        userService.loginUser("adrzej.kowalski@email.com", "Password");

        // when
        Result result = userService.loginUser("adrzej.kowalski@email.com", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @Test
    void shouldSuccessWhenCorrectCredentials() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");

        // when
        Result result = userService.loginUser("andrzej.kowalski@email.com", "Password");

        // then
        Assertions.assertTrue(result.getSuccess());
    }

    // logoutUser()
    @Test
    void shouldFailWhenUserNotLoggedIn() {
        // given
        User user = new User("1", "Andrzej Kowalski", "andrzej.kowalski@email.com", "Password");

        // when
        Result result = userService.logoutUser(user);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(user.getFullName() + " is not logged in.", result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldSuccessWhenUserLoggedIn() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");
        userService.loginUser("andrzej.kowalski@email.com", "Password");


        // when
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        Result result = userService.logoutUser(user);

        // then
        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals(user.getFullName() + " has been logged out successfully.",  result.getMessage());
    }

    // canBorrowBook()
    @Test
    void shouldReturnFalseWhenUserNotCorrect() {
        // given
        User user = new User("1", "Andrzej Kowalski", "andrzej.kowalski.email.com", "Password");

        // when
        Boolean canBorrowBook = userService.canBorrowBook(user);

        // then
        Assertions.assertFalse(canBorrowBook);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldReturnFalseWhenUserNotLoggedIn() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();

        // when
        Boolean canBorrowBook = userService.canBorrowBook(user);

        // then
        Assertions.assertFalse(canBorrowBook);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldReturnFalseWhenBorrowLimitExceeded() {
        // given
        BookService bookService = new BookService();
        LibraryService libraryService = new LibraryService(userService, bookService);
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        bookService.addBook("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley");
        bookService.addBook("1984", "George Orwell", 1949, "Secker & Warburg");
        bookService.addBook("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
        bookService.addBook("Design Patterns", "Erich Gamma", 1994, "Addison-Wesley");

        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        user.setLoggedIn(true);
        libraryService.borrowBook(user, bookService.getBooks().getFirst());
        libraryService.borrowBook(user, bookService.getBooks().get(1));
        libraryService.borrowBook(user, bookService.getBooks().get(2));
        libraryService.borrowBook(user, bookService.getBooks().get(3));
        libraryService.borrowBook(user, bookService.getBooks().get(4));

        // when
        Result result = libraryService.borrowBook(user, bookService.getBooks().get(5));

        // then
        Assertions.assertFalse(result.getSuccess());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void shouldReturnTrueWhenAllOkey() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        user.setLoggedIn(true);

        // when
        Boolean canBorrowBook = userService.canBorrowBook(user);

        // then
        Assertions.assertTrue(canBorrowBook);
    }

    // canReturnBook()
    @Test
    void canReturnBook_shouldReturnFalseWhenUserNotCorrect() {
        // given
        User user = new User("1", "Andrzej Kowalski", "andrzej.kowalski.email.com", "Password");
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // when
        Boolean canReturnBook = userService.canReturnBook(user, book);

        // then
        Assertions.assertFalse(canReturnBook);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void canReturnBook_shouldReturnFalseWhenUserNotLoggedIn() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // when
        Boolean canReturnBook = userService.canReturnBook(user, book);

        // then
        Assertions.assertFalse(canReturnBook);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void canReturnBook_shouldReturnFalseWhenBookNotBorrowed() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        user.setLoggedIn(true);
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // when
        Boolean canReturnBook = userService.canReturnBook(user, book);

        // then
        Assertions.assertFalse(canReturnBook);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void canReturnBook_shouldReturnTrueWhenAllOkey() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        user.setLoggedIn(true);
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        user.borrowBook(book);

        // when
        Boolean canReturnBook = userService.canReturnBook(user, book);

        // then
        Assertions.assertTrue(canReturnBook);
    }
}