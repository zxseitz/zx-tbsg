package ch.zxseitz.tbsg.server.api

import ch.zxseitz.tbsg.server.model.response.JwtResponse
import ch.zxseitz.tbsg.server.repo.IUserRepository
import ch.zxseitz.tbsg.server.model.Role
import ch.zxseitz.tbsg.server.model.User
import ch.zxseitz.tbsg.server.model.request.LoginRequest
import ch.zxseitz.tbsg.server.model.request.RegisterRequest
import ch.zxseitz.tbsg.server.security.JwtUtils

import java.util.Collections
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
class AuthController @Autowired constructor(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: IUserRepository,
    private val encoder: PasswordEncoder,
    private val jwtUtils: JwtUtils) {

    @PostMapping("/register")
    fun registerUser(@RequestBody registerRequest: RegisterRequest): ResponseEntity<Any> {
        if (!registerRequest.validate()) {
            return ResponseEntity.status(400).body("Invalid request")
        }
        if (userRepository.getByUsername(registerRequest.username) != null) {
            return ResponseEntity.status(400).body("Username already taken")
        }
        if (userRepository.getByEmail(registerRequest.email) != null) {
            return ResponseEntity.status(400).body("Email already taken")
        }
        userRepository.insert(User(ObjectId.get(), registerRequest.username,
                registerRequest.email, encoder.encode(registerRequest.password),
                listOf(Role.User.authority)
        ))
        return ResponseEntity.status(200).build()
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        if (!loginRequest.validate()) {
            return ResponseEntity.status(400).body("Invalid request")
        }
        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
        // todo attach user to authentication object
        val user = userRepository.getByUsername(loginRequest.username)
        if (user != null) {
            val jwt = jwtUtils.createJwt(user)
            return ResponseEntity.status(200).body(JwtResponse(jwt))
        }
        return ResponseEntity.status(400).build()
    }
}
