package ch.zxseitz.tbsg.games.reversi.core;

import java.util.*;
import java.util.function.Consumer;

public class ActionCollection {
    private final Map<Integer, Set<Integer>> actions;

    public ActionCollection() {
        actions = new TreeMap<>();
    }

    public ActionCollection(Map<Integer, Set<Integer>> values) {
        this();
        actions.putAll(values);
    }

    public void clear() {
        actions.clear();
    }

    public boolean anyIndices() {
        return actions.size() > 0;
    }

    public boolean containsIndex(int index) {
        return actions.containsKey(index);
    }

    public void add(int index, int... tokens) {
        var set = new TreeSet<Integer>();
        for (var i : tokens) {
            set.add(i);
        }
        add(index, set);
    }

    public Set<Integer> getIndices() {
        return actions.keySet();
    }

    public void add(int index, Set<Integer> integers) {
        actions.put(index, integers);
    }

    public Set<Integer> get(int index) {
        return actions.get(index);
    }

    public void foreach(int index, Consumer<Integer> callback) {
        var set = actions.get(index);
        if (set == null) {
            throw new IllegalArgumentException("collection does not contain index " + index);
        }
        set.forEach(callback);
    }
}
