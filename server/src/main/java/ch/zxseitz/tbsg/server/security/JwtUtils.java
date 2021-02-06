package ch.zxseitz.tbsg.server.security;

import ch.zxseitz.tbsg.server.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String secret;

    @Autowired
    public JwtUtils(@Value("${app.apikey.location}") String apiKeyPath) throws IOException {
        this.secret = Files.readString(Paths.get(apiKeyPath));
        logger.info("Loaded api key");
    }

    public String createJwt(User user) {
        var roles = user.getRoles().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((role1, role2) -> role1 + "," + role2).orElse("");
        return Jwts.builder()
                .claim("sub",user.getId())
                .claim("roles", roles)
                .claim(Claims.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public Optional<Map.Entry<ObjectId, Collection<GrantedAuthority>>> verifyJwt(String jwt) {
        try {
            var claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getBody();
            var expiration = claims.getExpiration();
            if (expiration.after(new Date())) {
                return Optional.of(Map.entry(new ObjectId(claims.getSubject()),
                        Arrays.stream(claims.get("roles", String.class).split(","))
                                .map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
            }
        } catch (MalformedJwtException e) {
            logger.warn("JWT is malformed {}", jwt);
        } catch (SignatureException e) {
            logger.warn("JWT has invalid signature {}", jwt);
        } catch (Exception e) {
            logger.warn("Error while validating jwt {}: {}", jwt, e.getMessage());
        }
        return Optional.empty();
    }
}

