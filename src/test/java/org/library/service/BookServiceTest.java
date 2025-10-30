package org.library.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.util.Result;

import java.util.ArrayList;
import java.util.List;

public class BookServiceTest {

    // addBook()
    @Test
    void shouldAddBookSuccessfully() {
        // given
        BookService bookService = new BookService();

        // when
        Result addBook = bookService.addBook(
                "Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // then
        Assertions.assertTrue(addBook.getSuccess());
        Assertions.assertEquals(1, bookService.getBooks().size());
        Assertions.assertEquals(1, bookService.getBooks().getFirst().getBookID());
    }

    @Test
    void shouldFailWhenTitleIsBlank() {
        // given
        BookService bookService = new BookService();

        // when
        Result addBook = bookService.addBook("", "Robert C. Martin", 2008, "Prentice Hall");

        // then
        Assertions.assertFalse(addBook.getSuccess());
        Assertions.assertEquals(0, bookService.getBooks().size());
    }

    @Test
    void shouldFailWhenAuthorIsBlank() {
        // given
        BookService bookService = new BookService();

        // when
        Result addBook = bookService.addBook("Clean Code", "", 2008, "Prentice Hall");

        // then
        Assertions.assertFalse(addBook.getSuccess());
        Assertions.assertEquals(0, bookService.getBooks().size());
    }

    @Test
    void shouldFailWhenPublisherIsBlank() {
        // given
        BookService bookService = new BookService();

        // when
        Result addBook = bookService.addBook("Clean Code", "Robert C. Martin", 2008, "");

        // then
        Assertions.assertFalse(addBook.getSuccess());
        Assertions.assertEquals(0, bookService.getBooks().size());
    }

    @Test
    void shouldHandleDuplicateAvailableBooks() {
        // given
        Book book1 =
                new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book2 =
                new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        BookService bookService = new BookService();

        // when
        Result firstAdd = bookService.addBook(
                book1.getTitle(), book1.getAuthor(), book1.getYear(), book1.getPublisher());
        Result secondAdd = bookService.addBook(
                book2.getTitle(), book2.getAuthor(), book2.getYear(), book2.getPublisher());

        // then
        Assertions.assertFalse(secondAdd.getSuccess());
        Assertions.assertEquals(1, bookService.getBooks().size());
        Assertions.assertEquals("Book already exists in the system", secondAdd.getMessage());
    }

//    @Test
//    void shouldIncrementBookIdAutomatically() {
//        // when
//        BookService bookService = new BookService();
//
//    }

    @Test
    void shouldNotModifyOriginalBookList() {
        // given
        BookService  bookService = new BookService();
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYear(), book.getPublisher());
        List<Book> books = new ArrayList<>(bookService.getBooks());

        // when
        Result result = bookService.addBook(book.getTitle(), book.getAuthor(), book.getYear(), book.getPublisher());

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(books, bookService.getBooks(),
                "Original book list should remain unchanged after duplicate attempt");

    }

    @Test
    void shouldReturnOnlyAvailableBooks() {
        // given
        Book book1 =
                new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book2 =
                new Book("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        Book book3 =
                new Book("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley");

        BookService bookService = getBookService(book1, book2, book3);
        bookService.getBooks().get(1).borrow();

        // when
        List<Book> availableBooks = bookService.listAvailableBooks();

        // then
        Assertions.assertEquals(2, availableBooks.size());
        Assertions.assertTrue(availableBooks.stream().allMatch(book -> book.getStatus() == BookStatus.AVAILABLE));
    }

    @Test
    void shouldReturnEmptyListWhenNoAvailableBooks() {
        // given
        Book book1 =
                new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book2 =
                new Book("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        Book book3 =
                new Book("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley");

        BookService bookService = getBookService(book1, book2, book3);
        bookService.getBooks().get(0).borrow();
        bookService.getBooks().get(1).borrow();
        bookService.getBooks().get(2).borrow();

        // when
        List<Book> availableBooks = bookService.listAvailableBooks();

        // then
        Assertions.assertEquals(0, availableBooks.size());
    }

    private static BookService getBookService(Book book1, Book book2, Book book3) {
        BookService bookService = new BookService();
        bookService.addBook(
                book1.getTitle(),
                book1.getAuthor(),
                book1.getYear(),
                book1.getPublisher()
        );
        bookService.addBook(
                book2.getTitle(),
                book2.getAuthor(),
                book2.getYear(),
                book2.getPublisher()
        );
        bookService.addBook(
                book3.getTitle(),
                book3.getAuthor(),
                book3.getYear(),
                book3.getPublisher()
        );
        return bookService;
    }
}