package ch.zxseitz.tbsg.model.request;

import java.util.regex.Pattern;

public class Validator {
    public static final Pattern usernameRegex = Pattern.compile("^\\w{6,20}$");
    public static final Pattern emailRegex = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    public static final Pattern passwordRegex = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[\\w@$!%*#?&]{8,20}$");

    private Validator() {}

    public static boolean checkUsername(String username) {
        return usernameRegex.matcher(username).matches();
    }

    public static boolean checkEmail(String email) {
        return emailRegex.matcher(email).matches();
    }

    public static boolean checkPassword(String password) {
        return passwordRegex.matcher(password).matches();
    }
}
