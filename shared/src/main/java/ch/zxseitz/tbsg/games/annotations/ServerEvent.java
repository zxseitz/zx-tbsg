package ch.zxseitz.tbsg.games.annotations;

import ch.zxseitz.tbsg.games.Color;
import ch.zxseitz.tbsg.games.GameState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ServerEvent {
    int code();
    String[] args();
}
