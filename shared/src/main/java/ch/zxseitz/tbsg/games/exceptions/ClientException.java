package ch.zxseitz.tbsg.games.exceptions;

import ch.zxseitz.tbsg.TbsgException;

public class ClientException extends TbsgException {
    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
