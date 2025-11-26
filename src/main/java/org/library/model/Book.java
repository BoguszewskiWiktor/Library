package org.library.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(exclude = {"bookID", "status"})
@RequiredArgsConstructor
@AllArgsConstructor
@Slf4j
public class Book {
    private Long bookID;
    @NonNull private String title;
    @NonNull private String author;
    @NonNull private Integer year;
    @NonNull private String publisher;
    private BookStatus status = BookStatus.AVAILABLE;

    public Boolean isAvailable() {
        log.debug("Checking if book '{}' is available", title);
        return status == BookStatus.AVAILABLE;
    }
    public void borrow() {
        log.info("Setting book {} status to BORROWED", title);
        this.status = BookStatus.BORROWED;
    }
    public void returnBack() {
        log.info("Setting book {} status to AVAILABLE", title);
        this.status = BookStatus.AVAILABLE;
    }
}