package ch.zxseitz.tbsg.dao;

import ch.zxseitz.tbsg.model.User;

import java.util.Optional;

public interface UserAccessObject extends DataAccessObject<User> {
    Optional<User> getByEmail(String email);
    Optional<User> getByUsername(String username);
}
