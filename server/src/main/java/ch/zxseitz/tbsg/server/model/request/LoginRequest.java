package ch.zxseitz.tbsg.server.model.request;

public class LoginRequest implements IRequest {
    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean validate() {
        return Validator.checkUsername(username)
                && Validator.checkPassword(password);
    }
}
