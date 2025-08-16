package com.pahanaedu.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification using Java's built-in security libraries.
 */
public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-512";
    private static final int ITERATIONS = 10000;
    
    /**
     * Hashes a password using SHA-512 with a random salt and multiple iterations.
     * 
     * @param password The password to hash
     * @return A string containing the salt and hash, separated by a colon
     */
    public static String hash(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password
            byte[] hash = hashWithSalt(password.toCharArray(), salt);
            
            // Encode salt and hash to Base64 for storage
            String saltStr = Base64.getEncoder().encodeToString(salt);
            String hashStr = Base64.getEncoder().encodeToString(hash);
            
            // Return the combined string
            return saltStr + ":" + hashStr;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies a password against a stored hash.
     * 
     * @param password The password to verify
     * @param storedHash The stored hash to verify against
     * @return True if the password matches, false otherwise
     */
    public static boolean verify(String password, String storedHash) {
        try {
            // Split the stored hash into salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            // Decode the salt and hash
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the input password with the same salt
            byte[] testHash = hashWithSalt(password.toCharArray(), salt);
            
            // Compare the hashes using a time-constant comparison
            return MessageDigest.isEqual(hash, testHash);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * For backward compatibility with existing code
     */
    public static String hashPassword(String password) {
        return hash(password);
    }
    
    /**
     * For backward compatibility with existing code
     */
    public static boolean verifyPassword(String password, String storedHash) {
        return verify(password, storedHash);
    }
    
    private static byte[] hashWithSalt(char[] password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.reset();
        digest.update(salt);
        
        byte[] input = new String(password).getBytes(StandardCharsets.UTF_8);
        byte[] result = digest.digest(input);
        
        // Apply multiple iterations of hashing
        for (int i = 0; i < ITERATIONS; i++) {
            digest.reset();
            result = digest.digest(result);
        }
        
        return result;
    }
}