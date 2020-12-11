package ch.zxseitz.tbsg.server.repo;

import ch.zxseitz.tbsg.server.model.User;

import java.util.Optional;

public interface IUserRepository extends IRepository<User> {
    Optional<User> getByEmail(String email);
    Optional<User> getByUsername(String username);
}
