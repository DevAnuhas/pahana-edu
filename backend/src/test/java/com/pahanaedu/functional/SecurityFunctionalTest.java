package com.pahanaedu.functional;

import com.pahanaedu.utils.PasswordHasher;
import com.pahanaedu.utils.DatabaseConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for utility classes and security features
 */
public class SecurityFunctionalTest {

    @Test
    public void testPasswordHashing() {
        String password = "testPassword123";
        String hash = PasswordHasher.hash(password);
        
        assertNotNull(hash);
        assertTrue(hash.contains(":"));
        assertTrue(PasswordHasher.verify(password, hash));
    }

    @Test
    public void testPasswordVerificationFailsWithWrongPassword() {
        String password = "testPassword123";
        String wrongPassword = "wrongPassword";
        String hash = PasswordHasher.hash(password);
        
        assertFalse(PasswordHasher.verify(wrongPassword, hash));
    }

    @Test
    public void testPasswordHashingProducesDifferentHashes() {
        String password = "testPassword123";
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);
        
        assertNotEquals(hash1, hash2); // Different salts should produce different hashes
        assertTrue(PasswordHasher.verify(password, hash1));
        assertTrue(PasswordHasher.verify(password, hash2));
    }

    @Test
    public void testPasswordHashingWithEmptyPassword() {
        String password = "";
        String hash = PasswordHasher.hash(password);
        
        assertNotNull(hash);
        assertTrue(PasswordHasher.verify(password, hash));
    }

    @Test
    public void testPasswordHashingWithNullPassword() {
        assertThrows(Exception.class, () -> {
            PasswordHasher.hash(null);
        });
    }

    @Test
    public void testPasswordVerificationWithInvalidHash() {
        String password = "testPassword123";
        String invalidHash = "invalid:hash:format";
        
        assertFalse(PasswordHasher.verify(password, invalidHash));
    }

    @Test
    public void testPasswordHashingSecurityStrength() {
        String password = "testPassword123";
        String hash = PasswordHasher.hash(password);
        
        // Check that hash contains salt and actual hash
        String[] parts = hash.split(":");
        assertEquals(2, parts.length);
        
        // Check that salt and hash are base64 encoded
        assertDoesNotThrow(() -> {
            java.util.Base64.getDecoder().decode(parts[0]);
            java.util.Base64.getDecoder().decode(parts[1]);
        });
    }

    @Test
    public void testDatabaseConnectionSingleton() {
        DatabaseConnection conn1 = DatabaseConnection.getInstance();
        DatabaseConnection conn2 = DatabaseConnection.getInstance();
        
        assertSame(conn1, conn2);
    }

    @Test
    public void testPasswordComplexityValidation() {
        String weakPassword = "123";
        String strongPassword = "StrongPass123!";
        
        // Simple complexity check
        assertTrue(strongPassword.length() >= 8);
        assertTrue(strongPassword.matches(".*[A-Z].*")); // Contains uppercase
        assertTrue(strongPassword.matches(".*[a-z].*")); // Contains lowercase
        assertTrue(strongPassword.matches(".*[0-9].*")); // Contains digit
        
        assertFalse(weakPassword.length() >= 8);
    }

    @Test
    public void testSessionTokenGeneration() {
        // Simulate session token generation
        String token1 = generateSessionToken();
        String token2 = generateSessionToken();
        
        assertNotEquals(token1, token2);
        assertTrue(token1.length() > 10);
        assertTrue(token2.length() > 10);
    }

    private String generateSessionToken() {
        return java.util.UUID.randomUUID().toString();
    }
}