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
public class MongoUserDetailsService implements UserDetailsService {
    private final IUserRepository userRepository;

    @Autowired
    public MongoUserDetailsService(@Qualifier("mongodb") IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.getByUsername(username);
        user.orElseThrow(() -> new UsernameNotFoundException(String.format("No user with username \"%s\" was found", username)));
        return user.map(UserDetailsImpl::create).get();
    }
}
