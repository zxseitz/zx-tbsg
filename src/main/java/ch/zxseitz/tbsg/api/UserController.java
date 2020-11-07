package ch.zxseitz.tbsg.api;

import ch.zxseitz.tbsg.dao.UserAccessObject;
import ch.zxseitz.tbsg.exceptions.NotFoundException;
import ch.zxseitz.tbsg.model.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/user")
public class UserController {
    private final UserAccessObject userAccessObject;

    @Autowired
    public UserController(@Qualifier("mongodb") UserAccessObject userAccessObject) {
        this.userAccessObject = userAccessObject;
    }

    @PostMapping
    public void addUser(@RequestBody User user) {
        //  password must be hashed!
        userAccessObject.insert(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userAccessObject.getAll();
    }

    @GetMapping(path = "{id}")
    public User getUser(@PathVariable("id") ObjectId id) {
        return userAccessObject.get(id)
                .orElseThrow(() -> new NotFoundException(String.format("No user found with id %s", id)));
    }

    @PutMapping(path = "{id}")
    public void updateUser(@PathVariable("id") ObjectId id, @RequestBody User user) {

    }

    @DeleteMapping(path = "{id}")
    public void updateUser(@PathVariable("id") ObjectId id) {

    }
}
