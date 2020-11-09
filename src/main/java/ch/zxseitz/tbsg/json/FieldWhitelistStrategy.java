package ch.zxseitz.tbsg.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldWhitelistStrategy implements ExclusionStrategy {
    private final Set<String> fields;

    public FieldWhitelistStrategy(String... fields) {
        this.fields = Arrays.stream(fields).collect(Collectors.toSet());
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return !this.fields.contains(f.getName());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
