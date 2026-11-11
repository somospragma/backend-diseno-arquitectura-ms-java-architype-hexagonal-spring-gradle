package co.com.pragma.accounts.infrastructure.helpers.masking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.com.pragma.accounts.model.sensitive.Sensitive;
import org.junit.jupiter.api.Test;

class DataMaskerTest {

    @Test
    void shouldMaskEmailKeepingDomain() {
        assertEquals("****@pragma.co", DataMasker.mask("ada.lovelace@pragma.co", Sensitive.Strategy.EMAIL));
    }

    @Test
    void shouldMaskKeepingLast4() {
        assertEquals("****7890", DataMasker.mask("1234567890", Sensitive.Strategy.LAST_4));
    }

    @Test
    void shouldMaskFully() {
        assertEquals("****", DataMasker.mask("supersecret", Sensitive.Strategy.FULL));
    }

    @Test
    void shouldReturnNullOrBlankUnchanged() {
        assertEquals(null, DataMasker.mask(null, Sensitive.Strategy.FULL));
        assertEquals("", DataMasker.mask("", Sensitive.Strategy.EMAIL));
    }

    @Test
    void shouldMaskShortValueFully() {
        assertEquals("****", DataMasker.mask("abc", Sensitive.Strategy.LAST_4));
    }
}
