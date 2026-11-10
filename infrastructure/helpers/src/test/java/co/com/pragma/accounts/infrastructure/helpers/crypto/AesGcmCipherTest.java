package co.com.pragma.accounts.infrastructure.helpers.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.SecureRandom;
import org.junit.jupiter.api.Test;

class AesGcmCipherTest {

    private byte[] key() {
        byte[] k = new byte[32];
        new SecureRandom().nextBytes(k);
        return k;
    }

    @Test
    void shouldEncryptAndDecryptRoundTrip() {
        AesGcmCipher cipher = new AesGcmCipher(key());
        String plaintext = "sensitive-value-123";
        String encrypted = cipher.encrypt(plaintext);
        assertNotEquals(plaintext, encrypted);
        assertEquals(plaintext, cipher.decrypt(encrypted));
    }

    @Test
    void shouldProduceDifferentCiphertextEachTime() {
        AesGcmCipher cipher = new AesGcmCipher(key());
        // IV aleatorio por operacion: mismo plaintext -> distinto ciphertext.
        assertNotEquals(cipher.encrypt("same"), cipher.encrypt("same"));
    }

    @Test
    void shouldRejectNon256BitKey() {
        assertThrows(IllegalArgumentException.class, () -> new AesGcmCipher(new byte[16]));
        assertThrows(IllegalArgumentException.class, () -> new AesGcmCipher(null));
    }

    @Test
    void shouldFailDecryptingTamperedOrInvalidCiphertext() {
        AesGcmCipher cipher = new AesGcmCipher(key());
        assertThrows(IllegalStateException.class, () -> cipher.decrypt("not-valid-base64-$$$"));
    }

    @Test
    void shouldFailDecryptingWithDifferentKey() {
        String encrypted = new AesGcmCipher(key()).encrypt("secret");
        assertThrows(IllegalStateException.class, () -> new AesGcmCipher(key()).decrypt(encrypted));
    }
}
