package co.com.pragma.accounts.infrastructure.helpers.masking;

import co.com.pragma.accounts.model.sensitive.Sensitive;

/**
 * Enmascaramiento de datos sensibles (DSC-O-002/003). Se usa para respuestas y
 * como base del masking de logs. Nunca revela el valor completo.
 */
public final class DataMasker {

    private DataMasker() {
    }

    public static String mask(String value, Sensitive.Strategy strategy) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return switch (strategy) {
            case LAST_4 -> maskLast4(value);
            case EMAIL -> maskEmail(value);
            case FULL -> "****";
        };
    }

    private static String maskLast4(String value) {
        if (value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }

    private static String maskEmail(String value) {
        int at = value.indexOf('@');
        if (at <= 0) {
            return "****";
        }
        String domain = value.substring(at);
        return "****" + domain;
    }
}
