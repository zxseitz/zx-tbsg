package ch.zxseitz.tbsg.api;

import ch.zxseitz.tbsg.model.response.JwtResponse;
import ch.zxseitz.tbsg.repo.IUserRepository;
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
        if (userRepository.getByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }
        if (userRepository.getByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }
        userRepository.insert(new User(ObjectId.get(), registerRequest.getUsername(),
                registerRequest.getEmail(), encoder.encode(registerRequest.getPassword()),
                Collections.singletonList(Role.User)));
        return ResponseEntity.status(200).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
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
