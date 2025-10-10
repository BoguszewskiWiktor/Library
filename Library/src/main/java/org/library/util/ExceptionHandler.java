package org.library.util;

import java.util.function.Supplier;

public class ExceptionHandler {

    /**
     * Wykonuje zadanie w trybie bezpiecznym — przechwytuje wszelkie wyjątki i wypisuje informację o błędzie,
     * zamiast przerywać program.
     *
     * @param task    kod do wykonania (np. lambda: {@code () -> doSomething()}). Nie powinien być {@code null}.
     * @param context krótki opis kontekstu (np. "Book validation"), używany przy logowaniu błędu;
     *                może być {@code null}, ale wtedy w logu nie będzie kontekstu.
     */

    public static Result safeRun(Runnable task, String context, Supplier<Result> onSuccess) {
        try {
            task.run();
            return onSuccess.get();
        } catch (Exception e) {
            System.err.printf("[ERROR] (%s): %s%n", context, e.getMessage());
            return Result.failure("Operation failed: " + context);
        }
    }

    /**
     * Wykonuje zadanie zwracające wartość; w przypadku wyjątku zwraca wartość zapasową.
     *
     * @param <T>     typ zwracany przez {@code task}.
     * @param task    kod (lambda) zwracający wartość; nie powinien być {@code null}.
     * @param fallback wartość zwracana, gdy wystąpi wyjątek; może być {@code null}.
     * @param context krótki opis kontekstu do logu.
     * @return wynik działania {@code task}, albo {@code fallback} gdy wystąpi wyjątek.
     */

    public static <T> T safeCall(Supplier<T> task, T fallback, String context) {
        try {
            return task.get();
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "No message";
            System.err.printf("[ERROR in %s] -> %s (%s)%n", context, errorMessage, e.getClass().getSimpleName());
            return fallback;
        }
    }
}