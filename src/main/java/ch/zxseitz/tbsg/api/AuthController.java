package ch.zxseitz.tbsg.api;

import ch.zxseitz.tbsg.dao.UserAccessObject;
import ch.zxseitz.tbsg.model.Role;
import ch.zxseitz.tbsg.model.User;
import ch.zxseitz.tbsg.model.request.LoginRequest;
import ch.zxseitz.tbsg.model.request.RegisterRequest;
import ch.zxseitz.tbsg.security.JwtUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserAccessObject userAccessObject;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userAccessObject.getByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }
        if (userAccessObject.getByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }
        userAccessObject.insert(new User(ObjectId.get(), registerRequest.getUsername(),
                registerRequest.getEmail(), encoder.encode(registerRequest.getPassword()),
                Collections.singletonList(Role.User)));
        return ResponseEntity.status(200).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        // todo attach user to authentication object
        var user = userAccessObject.getByUsername(loginRequest.getUsername());
        if (user.isPresent()) {
            var jwt = jwtUtils.createJwt(user.get());
            return ResponseEntity.status(200).body(jwt);
        }
        return ResponseEntity.status(400).build();
    }
}
