package ch.zxseitz.tbsg.server.api;

import ch.zxseitz.tbsg.server.repo.IUserRepository;
import ch.zxseitz.tbsg.server.model.Role;
import ch.zxseitz.tbsg.server.model.User;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/v1/user")
class UserController @Autowired constructor(@Qualifier("mongodb") private val userRepository: IUserRepository) {
    @PostMapping()
    fun addUser(@RequestBody user: User) {
        //  password must be hashed!
        userRepository.insert(user);
    }

    @GetMapping()
    fun getAllUsers(): ResponseEntity<List<User>> {
        return ResponseEntity.status(200).body(userRepository.getAll())
    }

    @GetMapping("{id}")
    fun getUser(@PathVariable("id") id: ObjectId): ResponseEntity<Any> {
        val auth = SecurityContextHolder.getContext().authentication
        if (!auth.authorities.contains(Role.Admin.authority)) {
            // non admin scope
            if (!auth.principal.equals(id)) {
                return ResponseEntity.status(403).body("Your not allowed to access this user")
            }
        }
        val user = userRepository.get(id)
        if (user != null) {
            return ResponseEntity.status(200).body(user)
        }
        return ResponseEntity.status(404).body("No user found with id ${id.toHexString()}")
    }

//    @PutMapping("{id}")
//    fun updateUser(@PathVariable("id") id: ObjectId, @RequestBody user: User): ResponseEntity<Any> {
//        return ResponseEntity.status(200).build();
//    }
//
//    @DeleteMapping("{id}")
//    fun deleteUser(@PathVariable("id") id: ObjectId): ResponseEntity<Any> {
//        return ResponseEntity.status(200).build();
//    }
}
