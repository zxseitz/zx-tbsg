package ch.zxseitz.tbsg.dao;

import ch.zxseitz.tbsg.model.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository("mongodb")
public class MongoImpl implements DataAccessObject<User> {
    private final MongoTemplate mongoTemplate;
    private final String collectionName = "users";

    @Autowired
    public MongoImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ObjectId insert(User user) {
        var inserted = mongoTemplate.insert(user, collectionName);
        return user.id;
    }

    @Override
    public List<User> getAll() {
        return mongoTemplate.findAll(User.class, collectionName);
    }

    @Override
    public User get(ObjectId id) {
        return mongoTemplate.findById(id, User.class, collectionName);
    }

    @Override
    public boolean update(ObjectId id, User user) {
        var query = query(where("_id").is(id));
        var update = new Update()
                .set("username", user.username);
        mongoTemplate.updateFirst(query, update, User.class, collectionName);
        return true;
    }

    @Override
    public boolean delete(ObjectId id) {
        var query = query(where("_id").is(id));
        mongoTemplate.remove(query, User.class, collectionName);
        return true;
    }
}
