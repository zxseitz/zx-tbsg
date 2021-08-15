package ch.zxseitz.tbsg.server.security;

import ch.zxseitz.tbsg.server.model.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

open class UserDetailsImpl(
    private val _username: String,
    private val _password: String,
    private val _authorities: List<GrantedAuthority>
) : UserDetails {
    companion object {
        fun create(user: User): UserDetailsImpl {
            return UserDetailsImpl(user.username, user.password, user.roles)
        }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return _authorities
    }

    override fun getPassword(): String {
        return _password
    }

    override fun getUsername(): String {
        return _username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
