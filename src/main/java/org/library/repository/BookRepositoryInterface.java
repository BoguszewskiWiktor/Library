package org.library.repository;

import org.library.model.Book;
import org.library.model.BookStatus;

import java.util.List;
import java.util.Optional;

public interface BookRepositoryInterface {
    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    List<Book> findAvailable();

    List<Book> findByTitle(String title);

    Boolean update(Book book);

    Boolean updateStatus(Long id, BookStatus status);

    Boolean delete(Long id);
}