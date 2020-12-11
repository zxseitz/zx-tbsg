package ch.zxseitz.tbsg.server.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class Role {
    public static final GrantedAuthority Admin = new SimpleGrantedAuthority("Admin");
    public static final GrantedAuthority User = new SimpleGrantedAuthority("User");
}
