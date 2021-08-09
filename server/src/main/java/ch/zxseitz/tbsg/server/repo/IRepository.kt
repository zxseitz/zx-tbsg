package ch.zxseitz.tbsg.server.repo;

import org.bson.types.ObjectId

interface IRepository<T> {
    fun insert(t: T)
    fun getAll(): List<T>
    fun get(id: ObjectId): T?
    fun update(id: ObjectId, t: T);
    fun delete(id: ObjectId);
}
