package ch.zxseitz.tbsg.games.annotations;

import ch.zxseitz.tbsg.games.Color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TbsgGame {
    String name();
    Color[] colors();
}
