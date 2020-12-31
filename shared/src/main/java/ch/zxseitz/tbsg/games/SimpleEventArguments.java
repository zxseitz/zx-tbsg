package ch.zxseitz.tbsg.games;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleEventArguments extends SimpleEvent {
    private final List<Object> args;

    public SimpleEventArguments(int code) {
        super(code);
        this.args = Collections.emptyList();
    }

    public SimpleEventArguments(int code, Object... args) {
        super(code);
        this.args = Arrays.asList(args);
    }

    @Override
    public <T> T getArgument(Class<T> tClass, int pos) {
        if (pos < args.size()) {
            var arg = args.get(pos);
            if (tClass.isInstance(arg)) {
                return tClass.cast(arg);
            }
        }
        return null;
    }
}
