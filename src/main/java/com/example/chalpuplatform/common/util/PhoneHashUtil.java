package com.example.chalpuplatform.common.util;

import com.example.chalpuplatform.common.exception.CouponException;
import com.example.chalpuplatform.common.exception.ErrorMessage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PhoneHashUtil {

    private static final String PHONE_SALT = "CHEFRIEND_COUPON_PHONE_SALT";
    private static final String PHONE_PATTERN = "^010\\d{8}$";

    public static String normalizePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new CouponException(ErrorMessage.COUPON_INVALID_PHONE_NUMBER);
        }

        String digits = phone.replaceAll("[^0-9]", "");

        if (!digits.matches(PHONE_PATTERN)) {
            throw new CouponException(ErrorMessage.COUPON_INVALID_PHONE_NUMBER);
        }

        return digits;
    }

    public static String hashPhone(String normalizedPhone) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPhone = normalizedPhone + PHONE_SALT;
            byte[] hash = digest.digest(saltedPhone.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static String normalizeAndHash(String phone) {
        String normalized = normalizePhone(phone);
        return hashPhone(normalized);
    }
}
