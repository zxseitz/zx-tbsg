package ch.zxseitz.tbsg.server.model

import ch.zxseitz.tbsg.games.IPlayer
import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority

@Document(collection = "users")
data class User(
    @Id private val _id: ObjectId,
    override val username: String,
    val email: String,
    @JsonIgnore val password: String,  // hashed
    val roles: List<GrantedAuthority>): IPlayer {

    override val id: String
        get() = _id.toHexString()
}
