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

    @SuppressWarnings("DataFlowIssue")
    @Test
    void registerUser_shouldThrowNullPointerExceptionWhenEmailIsNull() {
        // given, when, then
        Assertions.assertThrows(NullPointerException.class,
                () -> userService.registerUser(null, "Andrzej Kowalski", "Password"));
    }

    @Test
    void registerUser_shouldFailWhenEmailIsBlank() {
        // given, when
        Result result = userService.registerUser("     ", "Andrzej Kowalski", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("User email, full name, and password cannot be empty",  result.getMessage());
    }

    @Test
    void registerUser_shouldFailWhenEmailIsInvalid() {
        // given, when
        Result result = userService.registerUser("123", "Andrzej Kowalski", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("Invalid email format. Email address must contain @.",  result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void registerUser_shouldTrimAndNormalizeEmail() {
        // given, when
        Result result = userService.registerUser(
                "     AndrzejK@email.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("AndrzejK@email.com").get();

        // then
        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals("andrzejk@email.com", user.getEmail());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void registerUser_duplicateDetectionShouldBeCaseInsensitive() {
        // given
        Result result1 = userService.registerUser(
                "     AndrzejK@email.com", "Andrzej Kowalski", "Password1");
        User user = userService.getUserByEmail("AndrzejK@email.com").get();

        // when
        Result result2 = userService.registerUser(
                "ANDRZEJK@EMAIL.COM", "Andrzej Kowalski", "Password");

        // then
        Assertions.assertTrue(result1.getSuccess());
        Assertions.assertFalse(result2.getSuccess());
        Assertions.assertEquals(
                "User with email address " + user.getEmail() + " already exists.", result2.getMessage());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void registerUser_shouldThrowNullPointerExceptionWhenNameIsNull() {
        // given, when, then
        Assertions.assertThrows(NullPointerException.class,
                () -> userService.registerUser("randomEmail@email.com", null, "Password"));
    }

    @Test
    void registerUser_shouldFailWhenNameIsBlank() {
        // given, when
        Result result = userService.registerUser("randomEmail@email.com", "   ",  "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("User email, full name, and password cannot be empty", result.getMessage());
    }

    @Test
    void registerUser_shouldFailWhenFullNameDoesNotContainSpace() {
        // given, when
        Result result = userService.registerUser("andrzej@email.com", "AndrzejKowalski", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(
                "Invalid full name format. Full name must contain whitespace between name and surname.",
                result.getMessage()
        );
    }

    @Test
    void registerUser_shouldFailWhenPasswordTooShort() {
        // given, when
        Result result = userService.registerUser("Andrzej@email.com", "Andrzej Kowalski", "Pass");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("Invalid password length. Password should be at least 8 characters.",
                result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void registerUser_shouldFailWhenEmailAlreadyExists() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();

        // when
        Result result = userService.registerUser(
                "andrzej.kowalski@email.com", "Andrzej Kowalski2", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("User with email address " + user.getEmail() + " already exists.",
                result.getMessage());

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void registerUser_shouldSuccessWhenGivenValidInput() {
        // given, when
        Result result = userService.registerUser(
                "andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();

        // then
        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals("User " + user.getEmail() + " has been successfully registered.",
                result.getMessage());
    }

    @Test
    void loginUser_shouldFailWhenEmailNotFound() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");

        // when
        Result result = userService.loginUser("andrzejkowalski@email.com", "Password");

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("User with email andrzejkowalski@email.com does not exist.", result.getMessage());
    }

    @Test
    void loginUser_shouldFailWhenPasswordIncorrect() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");

        // when
        Result result = userService.loginUser("andrzej.kowalski@email.com", "Password1");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals("Invalid password.", result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void loginUser_shouldFailWhenUserAlreadyLoggedIn() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        userService.loginUser("andrzej.kowalski@email.com", "Password");

        // when
        Result result = userService.loginUser("andrzej.kowalski@email.com", "Password");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(user.getFullName() + " is already logged in.", result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void loginUser_shouldSuccessWhenCorrectCredentials() {
        // given
        userService.registerUser("andrzej.kowalski@email.com",  "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();

        // when
        Result result = userService.loginUser("andrzej.kowalski@email.com", "Password");

        // then
        Assertions.assertTrue(result.getSuccess());
        Assertions.assertEquals(user.getFullName() + " successfully logged in.", result.getMessage());
    }

    @Test
    void logoutUser_shouldFailWhenUserNotLoggedIn() {
        // given
        User user = new User(
                "1", "Andrzej Kowalski", "andrzej.kowalski@email.com", "Password");

        // when
        Result result = userService.logoutUser(user);

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(user.getFullName() + " is not logged in.", result.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void logoutUser_shouldSuccessWhenUserLoggedIn() {
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

    @Test
    void canBorrowBook_shouldReturnFalseWhenUserNotCorrect() {
        // given
        User user = new User(
                "1", "Andrzej Kowalski", "andrzej.kowalski.email.com", "Password");

        // when
        Boolean canBorrowBook = userService.canBorrowBook(user);

        // then
        Assertions.assertFalse(canBorrowBook);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void canBorrowBook_shouldReturnFalseWhenUserNotLoggedIn() {
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
    void canBorrowBook_shouldReturnFalseWhenBorrowLimitExceeded() {
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
    void canBorrowBook_shouldReturnTrueWhenAllOkey() {
        // given
        userService.registerUser("andrzej.kowalski@email.com", "Andrzej Kowalski", "Password");
        User user = userService.getUserByEmail("andrzej.kowalski@email.com").get();
        user.setLoggedIn(true);

        // when
        Boolean canBorrowBook = userService.canBorrowBook(user);

        // then
        Assertions.assertTrue(canBorrowBook);
    }

    @Test
    void canReturnBook_canReturnBook_shouldReturnFalseWhenUserNotCorrect() {
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
    void canReturnBook_canReturnBook_shouldReturnFalseWhenUserNotLoggedIn() {
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
    void canReturnBook_canReturnBook_shouldReturnFalseWhenBookNotBorrowed() {
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
    void canReturnBook_canReturnBook_shouldReturnTrueWhenAllOkey() {
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