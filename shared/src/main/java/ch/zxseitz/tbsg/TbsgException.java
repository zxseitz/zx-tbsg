package ch.zxseitz.tbsg;

public class TbsgException extends Exception {
    public TbsgException(String message) {
        super(message);
    }

    public TbsgException(String message, Throwable cause) {
        super(message, cause);
    }
}
