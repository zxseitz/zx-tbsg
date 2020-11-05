package ch.zxseitz.tbsg.api;

import ch.zxseitz.tbsg.dao.DataAccessObject;
import ch.zxseitz.tbsg.model.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/user")
@RestController
public class UserController {
    private final DataAccessObject<User> userDao;

    @Autowired
    public UserController(@Qualifier("mongodb") DataAccessObject<User> userDao) {
        this.userDao = userDao;
    }

    @PostMapping
    public void addUser(@RequestBody User user) {
        // todo hash password
        userDao.insert(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userDao.getAll();
    }

    @GetMapping(path = "{id}")
    public User getUser(@PathVariable("id") ObjectId id) {
        return userDao.get(id);
    }

    @PutMapping(path = "{id}")
    public void updateUser(@PathVariable("id") ObjectId id, @RequestBody User user) {
        userDao.update(id, user);
    }

    @DeleteMapping(path = "{id}")
    public void updateUser(@PathVariable("id") ObjectId id) {
        userDao.delete(id);
    }
}
