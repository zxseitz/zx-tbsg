package ch.zxseitz.tbsg.server.repo.mongo;

import ch.zxseitz.tbsg.server.model.User;
import ch.zxseitz.tbsg.server.repo.IUserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("mongodb")
public class MongoClientImpl implements IUserRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoClientImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void insert(User user) {
        mongoTemplate.insert(user);
    }

    @Override
    public List<User> getAll() {
        return mongoTemplate.findAll(User.class);
    }

    @Override
    public Optional<User> get(ObjectId id) {
        var query = Query.query(Criteria.where("_id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    @Override
    public Optional<User> getByEmail(String email) {
        var query = Query.query(Criteria.where("email").is(email));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    @Override
    public Optional<User> getByUsername(String username) {
        var query = Query.query(Criteria.where("username").is(username));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    @Override
    public void update(ObjectId id, User user) {
        var query = Query.query(Criteria.where("_id").is(id));
        var update = new Update()
                .set("username", user.getUsername());
        mongoTemplate.updateFirst(query, update, User.class);
    }

    @Override
    public void delete(ObjectId id) {
        var query = Query.query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, User.class);
    }
}
