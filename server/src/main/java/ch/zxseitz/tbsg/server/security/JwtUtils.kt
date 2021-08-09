package ch.zxseitz.tbsg.server.security

import ch.zxseitz.tbsg.server.model.User

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Date
import java.util.stream.Collectors
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

@Service
class JwtUtils @Autowired constructor(@Value("\${app.apikey.location}") apiKeyPath: String) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(JwtUtils::class.java)
    }

    private var secret: String

    init {
        this.secret = Files.readString(Paths.get(apiKeyPath))
        logger.info("Loaded api key")
    }

    fun createJwt(user: User): String {
        val roles = user.roles.stream()
                .map(GrantedAuthority::getAuthority)
                .reduce{role1, role2 -> "$role1,$role2" }.orElse("")
        return Jwts.builder()
                .claim("sub",user.id)
                .claim("roles", roles)
                .claim(Claims.EXPIRATION, Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secret).compact()
    }

    fun verifyJwt(jwt: String): Pair<ObjectId, Collection<GrantedAuthority>>? {
        try {
            val claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).body
            val expiration = claims.expiration
            if (expiration.after(Date())) {
                return Pair(ObjectId(claims.subject),
                        claims.get("roles", String::class.java).split(",").stream()
                                .map{authority -> SimpleGrantedAuthority(authority)}.collect(Collectors.toList()))
            }
        } catch (e: MalformedJwtException) {
            logger.warn("JWT is malformed $jwt")
        } catch (e: SignatureException) {
            logger.warn("JWT has invalid signature $jwt")
        } catch (e: Exception) {
            logger.warn("Error while validating jwt $jwt: ${e.message}")
        }
        return null
    }
}

