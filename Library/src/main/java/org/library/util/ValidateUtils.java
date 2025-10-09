package org.library.util;

import java.util.Map;
import java.util.Objects;

public class ValidateUtils {
    public static void requireNonNull (Map<String, Object> args) {
        args.forEach((name, value) -> {
            if (Objects.isNull(value)) {
                throw new NullPointerException(name + "cannot be null");
            }
        });
    }
}