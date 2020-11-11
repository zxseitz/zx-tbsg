package ch.zxseitz.tbsg.api;

import ch.zxseitz.tbsg.repo.IUserRepository;
import ch.zxseitz.tbsg.model.Role;
import ch.zxseitz.tbsg.model.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final IUserRepository userRepository;

    @Autowired
    public UserController(@Qualifier("mongodb") IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void addUser(@RequestBody User user) {
        //  password must be hashed!
        userRepository.insert(user);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.status(200).body(userRepository.getAll());
    }

    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getUser(@PathVariable("id") ObjectId id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(Role.Admin)) {
            // non admin scope
            if (!auth.getPrincipal().equals(id)) {
                return ResponseEntity.status(403).body("Your not allowed to access this user");
            }
        }
        var user = userRepository.get(id);
        if (user.isPresent()) {
            return ResponseEntity.status(200).body(user);
        }
        return ResponseEntity.status(404).body(String.format("No user found with id %s", id.toHexString()));
    }

    @RequestMapping(path = "{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateUser(@PathVariable("id") ObjectId id, @RequestBody User user) {
        return ResponseEntity.status(200).build();
    }

    @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUser(@PathVariable("id") ObjectId id) {
        return ResponseEntity.status(200).build();
    }
}
