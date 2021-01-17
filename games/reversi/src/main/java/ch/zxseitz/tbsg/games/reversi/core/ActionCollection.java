package ch.zxseitz.tbsg.games.reversi.core;

import java.util.*;

public class ActionCollection {
    private final Map<Integer, Collection<Integer>> actions;

    public ActionCollection() {
        actions = new TreeMap<>();
    }

    public ActionCollection(Map<Integer, Collection<Integer>> values) {
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

    public void add(int index, Collection<Integer> integers) {
        actions.put(index, integers);
    }

    public Collection<Integer> get(int index) {
        return actions.get(index);
    }
}
