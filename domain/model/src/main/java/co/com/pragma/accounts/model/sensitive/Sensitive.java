package co.com.pragma.accounts.model.sensitive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un campo como dato sensible (DSC-O-001). La clasificacion permite que
 * el masking de logs/respuestas sepa que enmascarar y que politica aplicar.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.METHOD})
public @interface Sensitive {

    Classification value() default Classification.PII;

    /** Estrategia de enmascaramiento a aplicar. */
    Strategy strategy() default Strategy.FULL;

    enum Classification {
        PII, PCI, CREDENTIAL, HEALTH, INTERNAL
    }

    enum Strategy {
        /** Reemplaza todo el valor por asteriscos. */
        FULL,
        /** Conserva los ultimos 4 caracteres (p.ej. PAN). */
        LAST_4,
        /** Enmascara el usuario de un email, conserva el dominio. */
        EMAIL
    }
}
