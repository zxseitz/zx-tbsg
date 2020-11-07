package ch.zxseitz.tbsg.security;

import ch.zxseitz.tbsg.dao.UserAccessObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("mongo_us")
public class UserDetailsServiceMongoImpl implements UserDetailsService {
    private final UserAccessObject userAccessObject;

    @Autowired
    public UserDetailsServiceMongoImpl(@Qualifier("mongodb") UserAccessObject userAccessObject) {
        this.userAccessObject = userAccessObject;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userAccessObject.getByUsername(username);
        user.orElseThrow(() -> new UsernameNotFoundException(String.format("No user with username \"%s\" was found", username)));
        return user.map(UserDetailsImpl::create).get();
    }
}
