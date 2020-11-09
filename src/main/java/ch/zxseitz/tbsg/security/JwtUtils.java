package ch.zxseitz.tbsg.security;

import ch.zxseitz.tbsg.json.FieldWhitelistStrategy;
import ch.zxseitz.tbsg.model.User;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import io.jsonwebtoken.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String secret;

    @Autowired
    public JwtUtils() throws IOException, URISyntaxException {
        this.secret = Files.readString(Paths.get(Objects.requireNonNull(JwtUtils.class.getClassLoader()
                .getResource("secret/apikey")).toURI()));
        logger.info("Loaded api key");
    }

    public String createJwt(User user) {
        return Jwts.builder()
                .claim("sub",user.id.toHexString())
                .claim("roles", String.join(",", user.getRoles()))
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

