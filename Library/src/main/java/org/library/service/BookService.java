package org.library.service;

import lombok.Data;
import lombok.NonNull;
import org.library.model.Book;
import org.library.model.BookStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookService {
    private final List<Book> books = new ArrayList<>();

    public List<Book> listAvailableBooks() {
        List<Book> availableBooks = books.stream()
                .filter(book -> book.getStatus() == BookStatus.AVAILABLE)
                .toList();

        if (!availableBooks.isEmpty()) {
            availableBooks.forEach(System.out::println);
        }

        return availableBooks;
    }

    public List<Book> searchBookByTitle(@NonNull String title) {
        List<Book> foundBooks = books.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .toList();

        if (foundBooks.isEmpty()) {
            System.out.printf("No books found with the given title %s%n", title);
        } else {
            foundBooks.forEach(System.out::println);
        }
        return foundBooks;
    }

    public Boolean isBookAvailable(@NonNull Book book) {
        if (!isBookCorrect(book)) {
            return false;
        }

        return book.getStatus() != BookStatus.BORROWED;
    }

    public Boolean isBookCorrect(@NonNull Book book) {
        return books.contains(book);
    }
}