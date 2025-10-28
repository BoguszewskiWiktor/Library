package org.library.service;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.library.model.Book;
import org.library.model.BookStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class BookService {
    private final List<Book> books = new ArrayList<>();

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