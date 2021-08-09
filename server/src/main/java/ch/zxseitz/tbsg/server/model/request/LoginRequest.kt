package ch.zxseitz.tbsg.server.model.request;

data class LoginRequest(
    val username: String,
    val password: String
) : IRequest {
    override fun validate(): Boolean {
        return checkUsername(username) && checkPassword(password);
    }
}
