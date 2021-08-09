package ch.zxseitz.tbsg.server.repo.mongo;

import ch.zxseitz.tbsg.server.repo.IUserRepository;
import ch.zxseitz.tbsg.server.security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("mongo_us")
class MongoUserDetailsService @Autowired constructor(
    @Qualifier("mongodb") private val userRepository: IUserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.getByUsername(username)
        user ?: throw UsernameNotFoundException("No user with username '$username' was found")
        return UserDetailsImpl.create(user)
    }
}
