package org.example.nexora.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class providing common helper methods across the application.
 */
@Slf4j
@Component
public class Utils {

    private static final ObjectMapper objectMapper;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    // ==================== JSON Utilities ====================

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON", e);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to object", e);
            return null;
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // ==================== ID Generation ====================

    public static String generateRandomId(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String generateNumericCode(int digits) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    // ==================== Hashing ====================

    public static String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return null;
        }
    }

    public static String hashMd5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // ==================== Validation ====================

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && username.length() <= 30
                && username.matches("^[a-zA-Z0-9_-]+$");
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // ==================== String Utilities ====================

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    public static String slugify(String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    public static String extractUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        return email.substring(0, email.indexOf("@"));
    }

    // ==================== Collection Utilities ====================

    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public static <T> Set<T> emptyIfNull(Set<T> set) {
        return set == null ? Collections.emptySet() : set;
    }

    public static <K, V> Map<K, V> emptyIfNull(Map<K, V> map) {
        return map == null ? Collections.emptyMap() : map;
    }

    public static <T> List<T> distinct(List<T> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    public static <T> List<T> safe(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    // ==================== Date/Time Utilities ====================

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return dateTime == null ? null : LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }

    public static boolean isExpired(LocalDateTime dateTime) {
        return dateTime == null || dateTime.isBefore(LocalDateTime.now());
    }

    // ==================== Number Utilities ====================

    public static BigDecimal round(BigDecimal value, int scale) {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
    }

    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    public static boolean isNegative(Number number) {
        return number != null && number.doubleValue() < 0;
    }

    // ==================== Masking Utilities ====================

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 2) {
            return "**@" + parts[1];
        }
        return username.substring(0, 2) + "***@" + parts[1];
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        return "****" + phone.substring(phone.length() - 4);
    }

    public static String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    // ==================== File Utilities ====================

    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public static String getFileNameWithoutExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    // ==================== Pagination Utilities ====================

    public static int calculateOffset(int page, int size) {
        return (page - 1) * size;
    }

    public static int calculateTotalPages(long totalItems, int size) {
        return (int) Math.ceil((double) totalItems / size);
    }

    public static boolean isValidPage(int page) {
        return page > 0;
    }

    public static boolean isValidSize(int size) {
        return size > 0 && size <= 100;
    }

    // ==================== Array Utilities ====================

    public static <T> T[] concatArrays(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}