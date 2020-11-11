package ch.zxseitz.tbsg.model.request;

public class RegisterRequest implements IRequest {
    private final String username;
    private final String email;
    private final String password;

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean validate() {
        return Validator.checkUsername(username)
                && Validator.checkEmail(email)
                && Validator.checkPassword(password);
    }
}
