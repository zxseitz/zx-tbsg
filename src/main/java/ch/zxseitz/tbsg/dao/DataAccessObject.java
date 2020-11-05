package ch.zxseitz.tbsg.dao;

import org.bson.types.ObjectId;

import java.util.List;

public interface DataAccessObject<T> {
    ObjectId insert(T t);
    List<T> getAll();
    T get(ObjectId id);
    boolean update(ObjectId id, T t);
    boolean delete(ObjectId id);
}
