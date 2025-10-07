package org.example;

import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;


@Data
public class Library {
    private final List<Book> books;
    private final UserManager userManager;
    private static final int MAX_BORROW_LIMIT = 5;

    public Result borrowBook(@NonNull User user, @NonNull Book book) {
        ValidateUtils.requireNonNull(Map.ofEntries(
                entry("user", user),
                entry("book", book)
        ));

        /*
        Na razie metoda ta jest zbędna, ponieważ znajdująca się w niej logika sprawdza, czy użytkownik ma konto.
        Ten sam efekt osiągamy w tym momencie mając optionalUser, który zwróci albo błąd, gdy nie znalazł użytkownika
        albo zwróci użytkownika, który jest zarejestrowany i może wypożyczać książki.
        W przyszłości, jeżeli logika metody isUserCorrect się rozrośnie to będzie potrzeba przywrócenia tej metody
         */

//        if (!isUserCorrect(user)) {
//            return Result.failure("User " +  user.getEmail() + " cannot borrow books.");
//        }

//        Sprawdzenie, czy książka jest poprawna
        if (!isBookCorrect(book)) {
            return Result.failure("Book '" + book.getTitle() + "' not found");
        }

//        Sprawdzenie limitu wypożyczeń
        if (user.getBorrowedBooks().size() == MAX_BORROW_LIMIT) {
            return Result.failure
                    ("You have reached the maximum number of books in the system: " + MAX_BORROW_LIMIT);
        }

//        Sprawdzenie, czy książka nie jest już wypożyczona
        if (book.getStatus().equals(BookStatus.BORROWED)) {
            return Result.failure("Book " + book.getTitle() + " is already borrowed");
        }

        book.setStatus(BookStatus.BORROWED);
        user.getBorrowedBooks().add(book);

        return Result.success("Book " + book.getTitle() + " is borrowed successfully by " + user.getEmail());
    }

    public Result returnBook(@NonNull User user, @NonNull Book book) {
        ValidateUtils.requireNonNull(Map.ofEntries(
                entry("user", user),
                entry("book", book)
        ));

        if (!user.getLoggedIn()) {
            return Result.failure("User " +  user.getEmail() + " must be logged in to return books.");
        }

//        Sprawdzenie, czy książka jest poprawna
        if (!isBookCorrect(book)) {
            return Result.failure("Book is not found in system.");
        }

        /*
        Na razie metoda ta jest zbędna, ponieważ znajdująca się w niej logika sprawdza, czy użytkownik ma konto.
        Ten sam efekt osiągamy w tym momencie mając optionalUser, który zwróci albo błąd, gdy nie znalazł użytkownika,
        albo zwróci użytkownika, który jest zarejestrowany i może zwracać książki.
        W przyszłości, jeżeli logika metody isUserCorrect się rozrośnie to będzie potrzeba przywrócenia tej metody
        if (!isUserCorrect(user)) {
            return Result.failure("User is not found in system");
        }
         */

//        Sprawdzenie, czy użytkownik ma wypożyczoną tę książkę
        if (!user.getBorrowedBooks().contains(book)) {
            return Result.failure
                    ("User " + user.getEmail() + " does not have book " + book.getTitle() + " borrowed");
        }

//        Zwrócenie książki
        book.setStatus(BookStatus.AVAILABLE);
        user.getBorrowedBooks().remove(book);

        return Result.success("Book " + book.getTitle() + " is returned successfully by " + user.getEmail());
    }

    public List<Book> listAvailableBooks() {
        List<Book> availableBooks = books.stream()
                .filter(book -> book.getStatus() == BookStatus.AVAILABLE)
                .toList();

        if (availableBooks.isEmpty()) {
            System.out.println("No books available at the moment");
        } else {
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

    public List<Book> getUserBorrowedBooks(@NonNull User user) {
        ValidateUtils.requireNonNull(Map.ofEntries(
                entry("user", user)
        ));
        List<Book> borrowed = user.getBorrowedBooks();
        if (borrowed.isEmpty()) {
            System.out.printf("User %s has no borrowed Books %n", user.getEmail());
        }
        return borrowed;
    }

    private Boolean isBookCorrect(@NonNull Book book) {
        ValidateUtils.requireNonNull(Map.ofEntries(
                entry("book", book)
        ));

        if (!books.contains(book)) {
            System.err.printf("Book '%s' not found %n", book.getTitle());
            return false;
        }
        return true;
    }

    private Boolean isUserCorrect(@NonNull User user) {
        ValidateUtils.requireNonNull(Map.ofEntries(
                entry("user", user)
        ));

        if (userManager.getUserByEmail(user.getEmail()).isEmpty()) {
            System.err.printf("User %s not found in system%n", user.getEmail());
            return false;
        }
        return true;
    }
}