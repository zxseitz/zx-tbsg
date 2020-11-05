package ch.zxseitz.tbsg.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Update;

@Document
public class User {
    @Id
    public final ObjectId id;

    public final String username;
    public final String password;  // hashed

    public User(ObjectId id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}
