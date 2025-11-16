package org.library.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.util.Result;

import java.util.ArrayList;
import java.util.List;

public class BookServiceTest {

    private BookService bookService;

    @BeforeEach
    void setup() {
        bookService = new BookService();
    }

    // addBook()
    @Test
    void shouldAddBookSuccessfully() {
        // given, when
        Result addBook = bookService.addBook(
                "Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // then
        Assertions.assertTrue(addBook.getSuccess());
        Assertions.assertEquals(1, bookService.getBooks().size());
        Assertions.assertEquals(1, bookService.getBooks().getFirst().getBookID());
    }

    @Test
    void shouldFailWhenTitleIsBlank() {
        // given, when
        Result addBook = bookService.addBook("", "Robert C. Martin", 2008, "Prentice Hall");

        // then
        Assertions.assertFalse(addBook.getSuccess());
        Assertions.assertEquals(0, bookService.getBooks().size());
    }

    @Test
    void shouldFailWhenAuthorIsBlank() {
        // given, when
        Result addBook = bookService.addBook("Clean Code", "", 2008, "Prentice Hall");

        // then
        Assertions.assertFalse(addBook.getSuccess());
        Assertions.assertEquals(0, bookService.getBooks().size());
    }

    @Test
    void shouldFailWhenPublisherIsBlank() {
        // given, when
        Result addBook = bookService.addBook("Clean Code", "Robert C. Martin", 2008, "");

        // then
        Assertions.assertFalse(addBook.getSuccess());
        Assertions.assertEquals(0, bookService.getBooks().size());
    }

    @Test
    void shouldHandleDuplicateAvailableBooks() {
        // given, when
        Result firstAdd = bookService.addBook
                ("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Result secondAdd = bookService.addBook
                ("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // then
        Assertions.assertTrue(firstAdd.getSuccess());
        Assertions.assertFalse(secondAdd.getSuccess());
        Assertions.assertEquals(1, bookService.getBooks().size());
        Assertions.assertEquals("Book already exists in the system", secondAdd.getMessage());
    }

    @Test
    void shouldIncrementBookIdAutomatically() {
        // given, when
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        bookService.addBook("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley");

        // then
        Assertions.assertEquals(List.of(1L, 2L, 3L),
                bookService.getBooks().stream().map(Book::getBookID).toList());
    }

    // listAvailableBooks()
    @Test
    void shouldReturnOnlyAvailableBooks() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        bookService.addBook("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley");

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
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        bookService.addBook("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley");

        bookService.getBooks().forEach(Book::borrow);

        // when
        List<Book> availableBooks = bookService.listAvailableBooks();

        // then
        Assertions.assertTrue(availableBooks.isEmpty());
    }

    // searchBookByTitle()
    @Test
    void shouldFindBooksByTitleIgnoringCase() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // when
        List<Book> result1 = bookService.searchBookByTitle("Clean Code");
        List<Book> result2 = bookService.searchBookByTitle("clean code");
        List<Book> result3 = bookService.searchBookByTitle("CLEAN CODE");

        // then
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, result1.size()),
                () -> Assertions.assertEquals(1, result2.size()),
                () -> Assertions.assertEquals(1, result3.size()),
                () -> Assertions.assertEquals(result1.getFirst(), result2.getFirst()),
                () -> Assertions.assertEquals(result1.getFirst(), result3.getFirst())
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoBooksFound() {
        // when
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // when
        List<Book> books = new ArrayList<>(bookService.searchBookByTitle("Clean"));

        // then
        Assertions.assertEquals(0, books.size());
    }

    // isBookAvailable(Book book)
    @Test
    void shouldReturnTrueWhenBookExistAndIsAvailable() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();

        // when
        Boolean bookAvailable = bookService.isBookAvailable(book);

        // then
        Assertions.assertTrue(bookAvailable);

    }

    @Test
    void shouldReturnFalseWhenBookIsBorrowed() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        Book book = bookService.getBooks().getFirst();
        book.borrow();

        // when
        Boolean bookAvailable = bookService.isBookAvailable(book);

        // then
        Assertions.assertFalse(bookAvailable);
    }

    @Test
    void shouldReturnFalseWhenBookNotInSystem() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // when
        Boolean bookAvailable = bookService.isBookAvailable
                (new Book("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley"));

        // then
        Assertions.assertFalse(bookAvailable);
    }

    // isBookCorrect(Book book)
    @Test
    void shouldReturnTrueWhenBookExists() {
        // given
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        book.setBookID(1L);
        bookService.getBooks().add(book);

        // when
        Boolean result = bookService.isBookCorrect(book);

        // then
        Assertions.assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenBookNotExists() {
        // given
        Book book = new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        book.setBookID(1L);

        // when
        Boolean result = bookService.isBookCorrect(book);

        // then
        Assertions.assertFalse(result);
    }

    // additional cases
    @Test
    void shouldNotModifyOriginalBookList() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        List<Book> books = new ArrayList<>(bookService.getBooks());

        // when
        Result result = bookService.addBook
                ("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");

        // then
        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(books, bookService.getBooks(),
                "Original book list should remain unchanged after duplicate attempt");

    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void shouldThrowExceptionWhenAddingBookWithNullTitle() {
        // given, when, then
        Assertions.assertThrows(NullPointerException.class,
                () -> bookService.addBook(null, "Robert C. Martin", 2008, "Prentice Hall"));
    }

    @Test
    void shouldMaintainUniqueIdsAcrossAdds() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley");
        bookService.addBook("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley");

        // when
        bookService.searchBookByTitle("Effective Java");
        bookService.getBooks().getFirst().borrow();
        bookService.getBooks().getFirst().returnBack();

        bookService.addBook("Refactoring", "Martin Fowler", 1999, "Addison-Wesley");

        // then
        List<Long> ids = bookService.getBooks()
                .stream()
                .map(Book::getBookID)
                .toList();

        long uniqueIds = ids.stream().distinct().count();

        Assertions.assertEquals(ids.size(), uniqueIds,
                "Each book should have a unique ID, even after multiple operations");

        Assertions.assertEquals(List.of(1L, 2L, 3L, 4L), ids,
                "Book IDs should increment sequentially across additions");
    }

    @Test
    void shouldHandleYearEdgeCases() {
        // given
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, "Prentice Hall");
        bookService.addBook("Effective Java", "Joshua Bloch", 1000, "Addison-Wesley");
        bookService.addBook("The Pragmatic Programmer", "Andrew Hunt", 1450, "Addison-Wesley");
        bookService.addBook("Refactoring", "Martin Fowler", 2025, "Addison-Wesley");
        bookService.addBook("To Kill a Mockingbird", "Harper Lee", 2300, "J.B. Lippincott & Co.");

        // when
        List<Book> result1 = bookService.searchBookByTitle("Effective Java");
        List<Book> result2 = bookService.searchBookByTitle("To Kill a Mockingbird");
        List<Book> result3 = bookService.searchBookByTitle("Clean Code");
        List<Book> result4 = bookService.searchBookByTitle("Refactoring");
        List<Book> result5 = bookService.searchBookByTitle("The Pragmatic Programmer");
        int size = bookService.getBooks().size();

        // then
        Assertions.assertTrue(result1.isEmpty());
        Assertions.assertTrue(result2.isEmpty());
        Assertions.assertFalse(result3.isEmpty());
        Assertions.assertFalse(result4.isEmpty());
        Assertions.assertFalse(result5.isEmpty());
        Assertions.assertEquals(3, size);
    }
}