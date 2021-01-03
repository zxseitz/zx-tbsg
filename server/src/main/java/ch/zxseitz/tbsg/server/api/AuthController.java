package ch.zxseitz.tbsg.server.api;

import ch.zxseitz.tbsg.server.model.response.JwtResponse;
import ch.zxseitz.tbsg.server.repo.IUserRepository;
import ch.zxseitz.tbsg.server.model.Role;
import ch.zxseitz.tbsg.server.model.User;
import ch.zxseitz.tbsg.server.model.request.LoginRequest;
import ch.zxseitz.tbsg.server.model.request.RegisterRequest;
import ch.zxseitz.tbsg.server.security.JwtUtils;

import java.util.Collections;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final IUserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, IUserRepository userRepository,
                          PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (!registerRequest.validate()) {
            return ResponseEntity.status(400).body("Invalid request");
        }
        if (userRepository.getByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }
        if (userRepository.getByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(400).body("Email already taken");
        }
        userRepository.insert(new User(ObjectId.get(), registerRequest.getUsername(),
                registerRequest.getEmail(), encoder.encode(registerRequest.getPassword()),
                Collections.singletonList(Role.User)));
        return ResponseEntity.status(200).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (!loginRequest.validate()) {
            return ResponseEntity.status(400).body("Invalid request");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        // todo attach user to authentication object
        var user = userRepository.getByUsername(loginRequest.getUsername());
        if (user.isPresent()) {
            var jwt = jwtUtils.createJwt(user.get());
            return ResponseEntity.status(200).body(new JwtResponse(jwt));
        }
        return ResponseEntity.status(400).build();
    }
}
