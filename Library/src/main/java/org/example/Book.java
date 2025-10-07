package org.example;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"status"})
@RequiredArgsConstructor
public class Book {
    @NonNull private Integer bookID;
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