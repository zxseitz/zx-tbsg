package ch.zxseitz.tbsg.server.model.request;

import java.util.regex.Pattern;

val usernameRegex: Pattern = Pattern.compile("^\\w{6,20}$");
val emailRegex: Pattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
val passwordRegex: Pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[\\w@$!%*#?&]{8,20}$");

fun checkUsername(username: String): Boolean {
    return usernameRegex.matcher(username).matches();
}

fun checkEmail(email: String): Boolean {
    return emailRegex.matcher(email).matches();
}

fun checkPassword(password: String): Boolean {
    return passwordRegex.matcher(password).matches();
}

