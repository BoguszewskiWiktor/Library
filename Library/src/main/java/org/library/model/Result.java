package org.library.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Result {
    private final Boolean success;
    private final String message;

    public static Result success(String message) {
        return new Result(true, message);
    }

    public static Result failure(String message) {
        return new Result(false, message);
    }

    @Override
    public String toString() {
        return (success ? "Success: " : "Failure: ") + message;
    }
}
