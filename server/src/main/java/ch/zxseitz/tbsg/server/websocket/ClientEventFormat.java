package ch.zxseitz.tbsg.server.websocket;

import java.lang.reflect.Method;

public class ClientEventFormat extends EventFormat {
    private final Method method;

    public ClientEventFormat(int code, ArgumentFormat[] args, Method method) {
        super(code, args);
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }
}
