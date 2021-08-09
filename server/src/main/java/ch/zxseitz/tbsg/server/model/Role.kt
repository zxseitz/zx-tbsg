package ch.zxseitz.tbsg.server.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

enum class Role(val authority: GrantedAuthority) {
    Admin(SimpleGrantedAuthority("Admin")),
    User(SimpleGrantedAuthority("User"))
}
