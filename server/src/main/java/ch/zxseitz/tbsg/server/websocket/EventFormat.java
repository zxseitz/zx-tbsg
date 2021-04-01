package ch.zxseitz.tbsg.server.websocket;

public class EventFormat {
    private final int code;
    private final ArgumentFormat[] args;

    public EventFormat(int code, ArgumentFormat[] args) {
        this.code = code;
        this.args = args;
    }

    public int getCode() {
        return code;
    }

    public ArgumentFormat[] getArgs() {
        return args;
    }
}
