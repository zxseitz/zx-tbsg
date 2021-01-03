package ch.zxseitz.tbsg.games;

import ch.zxseitz.tbsg.TbsgException;

public class EventException extends TbsgException {
    public EventException(String message) {
        super(message);
    }

    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
}
