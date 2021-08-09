package ch.zxseitz.tbsg.server.repo;

import ch.zxseitz.tbsg.server.model.User

interface IUserRepository: IRepository<User> {
    fun getByEmail(email: String): User?
    fun getByUsername(username: String): User?
}
