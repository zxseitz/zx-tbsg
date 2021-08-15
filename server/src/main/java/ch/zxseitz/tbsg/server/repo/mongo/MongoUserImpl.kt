package ch.zxseitz.tbsg.server.repo.mongo

import ch.zxseitz.tbsg.server.model.User
import ch.zxseitz.tbsg.server.repo.IUserRepository
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository("mongodb")
open class MongoUserImpl @Autowired constructor(private val mongoTemplate: MongoTemplate) : IUserRepository {
    override fun insert(user: User) {
        mongoTemplate.insert(user)
    }

    override fun getAll(): List<User> {
        return mongoTemplate.findAll(User::class.java)
    }

    override fun get(id: ObjectId): User? {
        val query = Query.query(Criteria.where("_id").`is`(id))
        return mongoTemplate.findOne(query, User::class.java)
    }

    override fun getByEmail(email: String): User? {
        val query = Query.query(Criteria.where("email").`is`(email))
        return mongoTemplate.findOne(query, User::class.java)
    }

    override fun getByUsername(username: String): User? {
        val query = Query.query(Criteria.where("username").`is`(username))
        return mongoTemplate.findOne(query, User::class.java)
    }

    override fun update(id: ObjectId, user: User) {
        val query = Query.query(Criteria.where("_id").`is`(id))
        val update = Update().set("username", user.username)
        mongoTemplate.updateFirst(query, update, User::class.java)
    }

    override fun delete(id: ObjectId) {
        val query = Query.query(Criteria.where("_id").`is`(id))
        mongoTemplate.remove(query, User::class.java)
    }
}
