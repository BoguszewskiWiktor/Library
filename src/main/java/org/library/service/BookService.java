package org.library.service;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.util.Result;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class BookService {
    private final List<Book> books = new ArrayList<>();
    private Long BOOK_ID = 0L;

    public Result addBook(@NonNull String title,
                          @NonNull String author,
                          @NonNull Integer year,
                          @NonNull String publisher) {

        long bookID = ++BOOK_ID;
        log.debug("Attempting to add new book with ID: {}, title: {}, author: {}",  bookID, title, author);

        if (title.isBlank() || author.isBlank() || publisher.isBlank()) {
            log.warn("Book validation failed - one or more required fields are empty " +
                    "(title : {}, author : {}, publisher : {})",  title, author, publisher);
            return Result.failure("Book title, author, publisher cannot be empty");
        }

        Book newBook = new Book(title, author, year, publisher);
        newBook.setBookID(bookID);

        if (books.contains(newBook)) {
            log.warn("Duplicate book detected: title={}, author={}, year={}, publisher={}",  title, author, year, publisher);
            return Result.failure("Book already exists in the system");
        }

        books.add(newBook);
        log.info("Book added successfully: title={}, author={}, year={}, publisher={}",  title, author, year, publisher);
        return Result.success("Book added successfully");
    }

    public List<Book> listAvailableBooks() {
        log.debug("Listing available books...");

        List<Book> availableBooks = books.stream()
                .filter(book -> book.getStatus() == BookStatus.AVAILABLE)
                .toList();

        if (availableBooks.isEmpty()) {
            log.info("No available books found.");
        } else {
            log.info("Found {} available books.", availableBooks.size());
            availableBooks.forEach(System.out::println);
        }

        return availableBooks;
    }

    public List<Book> searchBookByTitle(@NonNull String title) {
        log.info("Searching for books with title {}...", title);

        List<Book> foundBooks = books.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .toList();

        if (foundBooks.isEmpty()) {
            log.warn("No books found with the given tittle {}.", title);
        } else {
            log.info("Found {} book(s) matching tittle {}.", foundBooks.size(), title);
            foundBooks.forEach(book -> log.debug("Matched book: {}", book.getTitle()));
        }

        return foundBooks;
    }

    public Boolean isBookAvailable(@NonNull Book book) {
        log.debug("Checking if book {} is available.", book.getTitle());

        if (!isBookCorrect(book)) {
            log.warn("Book {} is not registered in the system.", book.getTitle());
            return false;
        }

        boolean available = book.getStatus() !=  BookStatus.BORROWED;
        log.info("Book {} availability: {}.", book.getTitle(), available);
        return available;
    }

    public Boolean isBookCorrect(@NonNull Book book) {
        boolean exists = books.contains(book);
        log.debug("Checking if book {} exists in the system: {}.", book.getTitle(), exists);
        return exists;
    }
}