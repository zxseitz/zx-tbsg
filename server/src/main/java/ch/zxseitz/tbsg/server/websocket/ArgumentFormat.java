package ch.zxseitz.tbsg.server.websocket;

public class ArgumentFormat {
    private final String name;
    private final Class<?> type;

    public ArgumentFormat(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }
}
