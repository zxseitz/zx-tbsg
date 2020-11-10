package ch.zxseitz.tbsg.repo;

import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface IRepository<T> {
    void insert(T t);
    List<T> getAll();
    Optional<T> get(ObjectId id);
    void update(ObjectId id, T t);
    void delete(ObjectId id);
}
