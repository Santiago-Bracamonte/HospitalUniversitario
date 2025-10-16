package com.example.hospitaluniversitario.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {


    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password, int minLength) {
        return !isEmpty(password) && password.length() >= minLength;
    }


    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}