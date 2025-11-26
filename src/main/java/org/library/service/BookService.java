package org.library.service;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.library.model.Book;
import org.library.model.BookStatus;
import org.library.repository.BookRepository;
import org.library.util.Result;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Data
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Result addBook(@NonNull String title,
                          @NonNull String author,
                          @NonNull Integer year,
                          @NonNull String publisher) {

        log.debug("Attempting to add new book with title: {}, author: {}", title, author);

        // 1. Validation
        if (title.isBlank() || author.isBlank() || publisher.isBlank()) {
            log.warn("Book validation failed - one or more required fields are empty " +
                    "(title : {}, author : {}, publisher : {})", title, author, publisher);
            return Result.failure("Book title, author, publisher cannot be empty");
        }

        int currentYear = LocalDate.now().getYear();
        if (year < 1450 || year > currentYear) {
            log.warn("Year out of bounds: year={}", year);
            return Result.failure("Year out of bounds");
        }

        // 2. Checking duplicate in DB
        boolean duplicatesExists = bookRepository.findByTitle(title)
                .stream()
                .anyMatch(book ->
                        book.getAuthor().equalsIgnoreCase(author)
                                && book.getPublisher().equalsIgnoreCase(publisher)
                                && book.getYear().equals(year)
                );

        if (duplicatesExists) {
            log.warn("Duplicate book detected: title={}, author={}, year={}, publisher={}",
                    title, author, year, publisher);
            return Result.failure("Book already exists in the system");
        }

        // 3. Creating new book
        Book newBook = new Book(title, author, year, publisher);
        newBook.setStatus(BookStatus.AVAILABLE);

        // 4. Save to the DB
        Book saved = bookRepository.save(newBook);

        log.info("Book added successfully: title={}, author={}, year={}, publisher={}",
                saved.getTitle(), saved.getAuthor(), saved.getYear(), saved.getPublisher());
        return Result.success("Book added successfully");
    }

    public List<Book> listAvailableBooks() {
        log.debug("Listing available books...");

        List<Book> availableBooks = bookRepository.findAvailable();

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

        List<Book> foundBooks = bookRepository.findByTitle(title);

        if (foundBooks.isEmpty()) {
            log.warn("No books found with the given title {}.", title);
        } else {
            log.info("Found {} book(s) matching tittle {}.", foundBooks.size(), title);
            foundBooks.forEach(book -> log.debug("Matched book: {}", book.getTitle()));
        }

        return foundBooks;
    }

    public List<Book> listAllBooks() {
        log.debug("Listing all books...");

        List<Book> allBooks = bookRepository.findAll();

        if (allBooks.isEmpty()) {
            log.warn("No books found.");
        }
        log.info("Found {} book(s).", allBooks.size());

        return allBooks;
    }

    public Boolean isBookAvailable(@NonNull Book book) {
        log.debug("Checking if book {} is available.", book.getTitle());

        List<Book> existing = bookRepository.findByTitle(book.getTitle())
                .stream()
                .filter(b ->
                        b.getAuthor().equalsIgnoreCase(book.getAuthor()) &&
                                b.getPublisher().equalsIgnoreCase(book.getPublisher()) &&
                                b.getYear().equals(book.getYear())
                )
                .toList();

        if (existing.isEmpty()) {
            log.warn("Book not found: title={}, author={}, year={}, publisher={}",
                    book.getTitle(), book.getAuthor(), book.getYear(), book.getPublisher());
            return false;
        }

        boolean available = existing.stream()
                .anyMatch(b -> b.getStatus().equals(BookStatus.AVAILABLE));

        if (available) {
            log.info("Book is available: {}", book.getTitle());
        } else {
            log.info("Book exists but is not available: {}", book.getTitle());
        }

        return available;
    }
}