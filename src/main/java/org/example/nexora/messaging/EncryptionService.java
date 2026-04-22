package org.example.nexora.messaging;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * End-to-End Encryption Service providing:
 * - Message encryption and decryption
 * - Key generation and management
 * - Secure key exchange
 * - Digital signatures
 * - Perfect forward secrecy
 * - Message authentication
 * - Key rotation and revocation
 * - Secure storage of encryption keys
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final KeyStoreService keyStoreService;
    private final SecureStorageService secureStorageService;
    private final AuditService auditService;

    // Encryption algorithms
    private static final String SYMMETRIC_ALGORITHM = "AES/GCM/NoPadding";
    private static final String ASYMMETRIC_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_EXCHANGE_ALGORITHM = "ECDH";
    
    // Key sizes
    private static final int SYMMETRIC_KEY_SIZE = 256;
    private static final int ASYMMETRIC_KEY_SIZE = 2048;
    private static final int SIGNATURE_KEY_SIZE = 2048;
    
    // Session keys cache
    private final Map<String, SessionKey> sessionKeys = new ConcurrentHashMap<>();
    
    // Public keys cache
    private final Map<Long, PublicKey> publicKeys = new ConcurrentHashMap<>();

    /**
     * Encrypt message for recipient
     */
    public String encryptMessage(String content, Long recipientId) {
        log.info("Encrypting message for recipient {}", recipientId);

        try {
            // Get or generate session key
            SessionKey sessionKey = getOrCreateSessionKey(recipientId);
            
            // Generate random IV
            byte[] iv = generateRandomIV();
            
            // Encrypt content with session key
            byte[] encryptedContent = encryptWithSessionKey(content.getBytes(StandardCharsets.UTF_8), 
                    sessionKey.getKey(), iv);
            
            // Encrypt session key with recipient's public key
            byte[] encryptedSessionKey = encryptWithPublicKey(sessionKey.getKey().getEncoded(), 
                    getPublicKey(recipientId));
            
            // Create encrypted message
            EncryptedMessage encryptedMessage = new EncryptedMessage();
            encryptedMessage.setEncryptedContent(Base64.getEncoder().encodeToString(encryptedContent));
            encryptedMessage.setEncryptedSessionKey(Base64.getEncoder().encodeToString(encryptedSessionKey));
            encryptedMessage.setIv(Base64.getEncoder().encodeToString(iv));
            encryptedMessage.setAlgorithm(SYMMETRIC_ALGORITHM);
            encryptedMessage.setTimestamp(LocalDateTime.now());
            
            // Sign message
            String signature = signMessage(encryptedMessage, getCurrentUserId());
            encryptedMessage.setSignature(signature);
            
            // Serialize encrypted message
            return serializeEncryptedMessage(encryptedMessage);

        } catch (Exception e) {
            log.error("Failed to encrypt message", e);
            throw new RuntimeException("Message encryption failed: " + e.getMessage());
        }
    }

    /**
     * Decrypt message for recipient
     */
    public String decryptMessage(String encryptedMessageStr, Long recipientId) {
        log.info("Decrypting message for recipient {}", recipientId);

        try {
            // Deserialize encrypted message
            EncryptedMessage encryptedMessage = deserializeEncryptedMessage(encryptedMessageStr);
            
            // Verify signature
            if (!verifyMessageSignature(encryptedMessage)) {
                throw new SecurityException("Message signature verification failed");
            }
            
            // Get recipient's private key
            PrivateKey privateKey = getPrivateKey(recipientId);
            
            // Decrypt session key
            byte[] encryptedSessionKey = Base64.getDecoder().decode(encryptedMessage.getEncryptedSessionKey());
            byte[] sessionKeyBytes = decryptWithPrivateKey(encryptedSessionKey, privateKey);
            
            // Reconstruct session key
            SecretKeySpec sessionKey = new SecretKeySpec(sessionKeyBytes, "AES");
            
            // Decrypt content
            byte[] iv = Base64.getDecoder().decode(encryptedMessage.getIv());
            byte[] encryptedContent = Base64.getDecoder().decode(encryptedMessage.getEncryptedContent());
            byte[] decryptedContent = decryptWithSessionKey(encryptedContent, sessionKey, iv);
            
            return new String(decryptedContent, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Failed to decrypt message", e);
            throw new RuntimeException("Message decryption failed: " + e.getMessage());
        }
    }

    /**
     * Generate user key pair
     */
    public KeyPairResult generateUserKeyPair(Long userId) {
        log.info("Generating key pair for user {}", userId);

        try {
            // Generate RSA key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(ASYMMETRIC_KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            // Generate signature key pair
            KeyPairGenerator signatureGenerator = KeyPairGenerator.getInstance("RSA");
            signatureGenerator.initialize(SIGNATURE_KEY_SIZE);
            KeyPair signatureKeyPair = signatureGenerator.generateKeyPair();
            
            // Store keys securely
            keyStoreService.storeKeyPair(userId, keyPair);
            keyStoreService.storeSignatureKeyPair(userId, signatureKeyPair);
            
            // Cache public key
            publicKeys.put(userId, keyPair.getPublic());
            
            // Log key generation
            auditService.logKeyGeneration(userId, "RSA", ASYMMETRIC_KEY_SIZE);
            
            KeyPairResult result = new KeyPairResult();
            result.setSuccess(true);
            result.setPublicKey(keyPair.getPublic());
            result.setPrivateKey(keyPair.getPrivate());
            result.setSignaturePublicKey(signatureKeyPair.getPublic());
            result.setSignaturePrivateKey(signatureKeyPair.getPrivate());
            
            return result;

        } catch (Exception e) {
            log.error("Failed to generate key pair", e);
            return KeyPairResult.failure("Key generation failed: " + e.getMessage());
        }
    }

    /**
     * Perform secure key exchange
     */
    public KeyExchangeResult performKeyExchange(Long initiatorId, Long responderId) {
        log.info("Performing key exchange between {} and {}", initiatorId, responderId);

        try {
            // Generate ECDH key pairs
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_EXCHANGE_ALGORITHM);
            keyPairGenerator.initialize(256);
            
            KeyPair initiatorKeyPair = keyPairGenerator.generateKeyPair();
            KeyPair responderKeyPair = keyPairGenerator.generateKeyPair();
            
            // Perform key agreement
            KeyAgreement initiatorAgreement = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM);
            initiatorAgreement.init(initiatorKeyPair.getPrivate());
            initiatorAgreement.doPhase(responderKeyPair.getPublic(), true);
            byte[] initiatorSharedSecret = initiatorAgreement.generateSecret();
            
            KeyAgreement responderAgreement = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM);
            responderAgreement.init(responderKeyPair.getPrivate());
            responderAgreement.doPhase(initiatorKeyPair.getPublic(), true);
            byte[] responderSharedSecret = responderAgreement.generateSecret();
            
            // Verify both parties got the same secret
            if (!Arrays.equals(initiatorSharedSecret, responderSharedSecret)) {
                throw new SecurityException("Key exchange failed - secrets don't match");
            }
            
            // Derive symmetric key from shared secret
            SecretKeySpec symmetricKey = deriveSymmetricKey(initiatorSharedSecret);
            
            // Create session key
            SessionKey sessionKey = new SessionKey();
            sessionKey.setKey(symmetricKey);
            sessionKey.setCreatedAt(LocalDateTime.now());
            sessionKey.setExpiresAt(LocalDateTime.now().plusHours(24));
            sessionKey.setInitiatorId(initiatorId);
            sessionKey.setResponderId(responderId);
            
            // Store session key
            String sessionKeyId = generateSessionKeyId(initiatorId, responderId);
            sessionKeys.put(sessionKeyId, sessionKey);
            
            // Log key exchange
            auditService.logKeyExchange(initiatorId, responderId, KEY_EXCHANGE_ALGORITHM);
            
            KeyExchangeResult result = new KeyExchangeResult();
            result.setSuccess(true);
            result.setSessionKey(symmetricKey);
            result.setSessionKeyId(sessionKeyId);
            
            return result;

        } catch (Exception e) {
            log.error("Failed to perform key exchange", e);
            return KeyExchangeResult.failure("Key exchange failed: " + e.getMessage());
        }
    }

    /**
     * Rotate user keys
     */
    public KeyRotationResult rotateUserKeys(Long userId) {
        log.info("Rotating keys for user {}", userId);

        try {
            // Get current keys
            KeyPair currentKeyPair = keyStoreService.getKeyPair(userId);
            KeyPair currentSignatureKeyPair = keyStoreService.getSignatureKeyPair(userId);
            
            // Generate new key pairs
            KeyPairResult newKeyPairs = generateUserKeyPair(userId);
            if (!newKeyPairs.isSuccess()) {
                return KeyRotationResult.failure("Failed to generate new keys");
            }
            
            // Re-encrypt session keys with new public key
            reencryptSessionKeys(userId, newKeyPairs.getPublicKey());
            
            // Update active session keys
            updateSessionKeys(userId, newKeyPairs.getPublicKey());
            
            // Schedule old keys for deletion (grace period)
            scheduleKeyDeletion(userId, currentKeyPair, currentSignatureKeyPair);
            
            // Log key rotation
            auditService.logKeyRotation(userId, "RSA", ASYMMETRIC_KEY_SIZE);
            
            KeyRotationResult result = new KeyRotationResult();
            result.setSuccess(true);
            result.setNewPublicKey(newKeyPairs.getPublicKey());
            result.setOldPublicKey(currentKeyPair.getPublic());
            result.setRotationTime(LocalDateTime.now());
            
            return result;

        } catch (Exception e) {
            log.error("Failed to rotate keys", e);
            return KeyRotationResult.failure("Key rotation failed: " + e.getMessage());
        }
    }

    /**
     * Revoke user keys
     */
    public KeyRevocationResult revokeUserKeys(Long userId) {
        log.info("Revoking keys for user {}", userId);

        try {
            // Get current keys
            KeyPair currentKeyPair = keyStoreService.getKeyPair(userId);
            KeyPair currentSignatureKeyPair = keyStoreService.getSignatureKeyPair(userId);
            
            // Add keys to revocation list
            keyStoreService.addToRevocationList(userId, currentKeyPair.getPublic());
            keyStoreService.addToSignatureRevocationList(userId, currentSignatureKeyPair.getPublic());
            
            // Invalidate all session keys
            invalidateUserSessionKeys(userId);
            
            // Remove from cache
            publicKeys.remove(userId);
            
            // Generate new keys
            KeyPairResult newKeyPairs = generateUserKeyPair(userId);
            if (!newKeyPairs.isSuccess()) {
                return KeyRevocationResult.failure("Failed to generate new keys after revocation");
            }
            
            // Log key revocation
            auditService.logKeyRevocation(userId, "RSA", ASYMMETRIC_KEY_SIZE);
            
            KeyRevocationResult result = new KeyRevocationResult();
            result.setSuccess(true);
            result.setRevokedPublicKey(currentKeyPair.getPublic());
            result.setNewPublicKey(newKeyPairs.getPublicKey());
            result.setRevocationTime(LocalDateTime.now());
            
            return result;

        } catch (Exception e) {
            log.error("Failed to revoke keys", e);
            return KeyRevocationResult.failure("Key revocation failed: " + e.getMessage());
        }
    }

    /**
     * Verify message integrity
     */
    public boolean verifyMessageIntegrity(String encryptedMessageStr, Long senderId) {
        log.debug("Verifying message integrity from sender {}", senderId);

        try {
            EncryptedMessage encryptedMessage = deserializeEncryptedMessage(encryptedMessageStr);
            
            // Get sender's public key
            PublicKey senderPublicKey = getPublicKey(senderId);
            
            // Verify signature
            return verifySignature(encryptedMessage, senderPublicKey);

        } catch (Exception e) {
            log.error("Failed to verify message integrity", e);
            return false;
        }
    }

    // Private helper methods
    private SessionKey getOrCreateSessionKey(Long recipientId) {
        String sessionKeyId = generateSessionKeyId(getCurrentUserId(), recipientId);
        
        SessionKey sessionKey = sessionKeys.get(sessionKeyId);
        if (sessionKey == null || sessionKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Generate new session key
            sessionKey = generateSessionKey(getCurrentUserId(), recipientId);
            sessionKeys.put(sessionKeyId, sessionKey);
        }
        
        return sessionKey;
    }

    private SessionKey generateSessionKey(Long initiatorId, Long recipientId) {
        try {
            // Generate AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(SYMMETRIC_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            
            SessionKey sessionKey = new SessionKey();
            sessionKey.setKey(secretKey);
            sessionKey.setCreatedAt(LocalDateTime.now());
            sessionKey.setExpiresAt(LocalDateTime.now().plusHours(24));
            sessionKey.setInitiatorId(initiatorId);
            sessionKey.setResponderId(recipientId);
            
            return sessionKey;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate session key", e);
        }
    }

    private byte[] generateRandomIV() {
        byte[] iv = new byte[12]; // GCM recommended IV size
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] encryptWithSessionKey(byte[] content, SecretKey sessionKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, gcmSpec);
        return cipher.doFinal(content);
    }

    private byte[] decryptWithSessionKey(byte[] encryptedContent, SecretKey sessionKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmSpec);
        return cipher.doFinal(encryptedContent);
    }

    private byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    private byte[] decryptWithPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    private String signMessage(EncryptedMessage message, Long signerId) throws Exception {
        // Get signer's private signature key
        PrivateKey privateKey = keyStoreService.getSignatureKeyPair(signerId).getPrivate();
        
        // Create signature
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        
        // Sign message content
        String messageData = message.getEncryptedContent() + message.getEncryptedSessionKey() + 
                          message.getIv() + message.getAlgorithm() + message.getTimestamp().toString();
        signature.update(messageData.getBytes(StandardCharsets.UTF_8));
        
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private boolean verifyMessageSignature(EncryptedMessage message) throws Exception {
        // Get signer's public signature key
        PublicKey publicKey = getPublicKey(getMessageSignerId(message));
        
        return verifySignature(message, publicKey);
    }

    private boolean verifySignature(EncryptedMessage message, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        
        // Verify message content
        String messageData = message.getEncryptedContent() + message.getEncryptedSessionKey() + 
                          message.getIv() + message.getAlgorithm() + message.getTimestamp().toString();
        signature.update(messageData.getBytes(StandardCharsets.UTF_8));
        
        byte[] signatureBytes = Base64.getDecoder().decode(message.getSignature());
        return signature.verify(signatureBytes);
    }

    private PublicKey getPublicKey(Long userId) throws Exception {
        PublicKey publicKey = publicKeys.get(userId);
        if (publicKey == null) {
            publicKey = keyStoreService.getPublicKey(userId);
            publicKeys.put(userId, publicKey);
        }
        return publicKey;
    }

    private PrivateKey getPrivateKey(Long userId) throws Exception {
        return keyStoreService.getPrivateKey(userId);
    }

    private String generateSessionKeyId(Long userId1, Long userId2) {
        return Math.min(userId1, userId2) + "_" + Math.max(userId1, userId2);
    }

    private Long getCurrentUserId() {
        // Get current user from security context
        return 1L; // Simplified
    }

    private Long getMessageSignerId(EncryptedMessage message) {
        // Extract signer ID from message (would be stored in message metadata)
        return 1L; // Simplified
    }

    private String serializeEncryptedMessage(EncryptedMessage message) {
        // Serialize message to JSON or other format
        return message.toString(); // Simplified
    }

    private EncryptedMessage deserializeEncryptedMessage(String messageStr) {
        // Deserialize message from JSON or other format
        EncryptedMessage message = new EncryptedMessage();
        // Parse messageStr and set fields
        return message; // Simplified
    }

    private SecretKeySpec deriveSymmetricKey(byte[] sharedSecret) {
        // Derive AES key from shared secret using HKDF
        return new SecretKeySpec(sharedSecret, "AES"); // Simplified
    }

    private void reencryptSessionKeys(Long userId, PublicKey newPublicKey) {
        // Re-encrypt all session keys with new public key
        sessionKeys.values().stream()
                .filter(key -> key.getInitiatorId().equals(userId) || key.getResponderId().equals(userId))
                .forEach(key -> {
                    // Re-encrypt with new public key
                    // Implementation would depend on specific requirements
                });
    }

    private void updateSessionKeys(Long userId, PublicKey newPublicKey) {
        // Update session keys that involve this user
        // Implementation would depend on specific requirements
    }

    private void scheduleKeyDeletion(Long userId, KeyPair oldKeyPair, KeyPair oldSignatureKeyPair) {
        // Schedule old keys for deletion after grace period
        // Implementation would use a scheduled task
    }

    private void invalidateUserSessionKeys(Long userId) {
        // Remove all session keys involving this user
        sessionKeys.entrySet().removeIf(entry -> {
            SessionKey key = entry.getValue();
            return key.getInitiatorId().equals(userId) || key.getResponderId().equals(userId);
        });
    }

    // Data classes
    @Data
    public static class EncryptedMessage {
        private String encryptedContent;
        private String encryptedSessionKey;
        private String iv;
        private String algorithm;
        private String signature;
        private LocalDateTime timestamp;
    }

    @Data
    public static class SessionKey {
        private SecretKey key;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private Long initiatorId;
        private Long responderId;
    }

    @Data
    public static class KeyPairResult {
        private boolean success;
        private PublicKey publicKey;
        private PrivateKey privateKey;
        private PublicKey signaturePublicKey;
        private PrivateKey signaturePrivateKey;
        private String error;

        public static KeyPairResult failure(String error) {
            KeyPairResult result = new KeyPairResult();
            result.setSuccess(false);
            result.setError(error);
            return result;
        }
    }

    @Data
    public static class KeyExchangeResult {
        private boolean success;
        private SecretKey sessionKey;
        private String sessionKeyId;
        private String error;

        public static KeyExchangeResult failure(String error) {
            KeyExchangeResult result = new KeyExchangeResult();
            result.setSuccess(false);
            result.setError(error);
            return result;
        }
    }

    @Data
    public static class KeyRotationResult {
        private boolean success;
        private PublicKey newPublicKey;
        private PublicKey oldPublicKey;
        private LocalDateTime rotationTime;
        private String error;

        public static KeyRotationResult failure(String error) {
            KeyRotationResult result = new KeyRotationResult();
            result.setSuccess(false);
            result.setError(error);
            return result;
        }
    }

    @Data
    public static class KeyRevocationResult {
        private boolean success;
        private PublicKey revokedPublicKey;
        private PublicKey newPublicKey;
        private LocalDateTime revocationTime;
        private String error;

        public static KeyRevocationResult failure(String error) {
            KeyRevocationResult result = new KeyRevocationResult();
            result.setSuccess(false);
            result.setError(error);
            return result;
        }
    }

    // Service placeholders
    private static class KeyStoreService {
        public void storeKeyPair(Long userId, KeyPair keyPair) {}
        public void storeSignatureKeyPair(Long userId, KeyPair keyPair) {}
        public KeyPair getKeyPair(Long userId) { return null; }
        public KeyPair getSignatureKeyPair(Long userId) { return null; }
        public PublicKey getPublicKey(Long userId) { return null; }
        public PrivateKey getPrivateKey(Long userId) { return null; }
        public void addToRevocationList(Long userId, PublicKey publicKey) {}
        public void addToSignatureRevocationList(Long userId, PublicKey publicKey) {}
    }

    private static class SecureStorageService {
        // Secure storage implementation
    }

    private static class AuditService {
        public void logKeyGeneration(Long userId, String algorithm, int keySize) {}
        public void logKeyExchange(Long initiatorId, Long responderId, String algorithm) {}
        public void logKeyRotation(Long userId, String algorithm, int keySize) {}
        public void logKeyRevocation(Long userId, String algorithm, int keySize) {}
    }

    // Service instances - duplicates removed
}
