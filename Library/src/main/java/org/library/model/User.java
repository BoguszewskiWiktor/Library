package org.library.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"borrowedBooks"})
@ToString(exclude = {"borrowedBooks"})
@RequiredArgsConstructor
public class User {
    private String userId;
    @NonNull
    private String fullName;
    @NonNull
    private String email;
    private String password;
    private List<Book> borrowedBooks = new ArrayList<>();
    private Boolean loggedIn = false;

    public User(String userId, @NonNull String fullName, @NonNull String email, String password) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public Boolean isLoggedIn() {
        return loggedIn;
    }

    public Boolean hasBorrowed(Book book) {
        return borrowedBooks.contains(book);
    }

    public void borrowBook(Book book) {
        borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
    }
}