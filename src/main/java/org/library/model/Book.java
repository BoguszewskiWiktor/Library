package org.library.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(exclude = {"bookID", "status"})
@RequiredArgsConstructor
public class Book {
    private Long bookID;
    @NonNull private String title;
    @NonNull private String author;
    @NonNull private Integer year;
    @NonNull private String publisher;
    private BookStatus status = BookStatus.AVAILABLE;

    public Boolean isAvailable() {
        return status == BookStatus.AVAILABLE;
    }
    public void borrow() {
        this.status = BookStatus.BORROWED;
    }
    public void returnBack() {
        this.status = BookStatus.AVAILABLE;
    }
}