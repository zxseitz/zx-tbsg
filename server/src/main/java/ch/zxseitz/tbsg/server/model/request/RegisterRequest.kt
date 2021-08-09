package ch.zxseitz.tbsg.server.model.request;

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
) : IRequest {
    override fun validate(): Boolean {
        return checkUsername(username) && checkEmail(email) && checkPassword(password)
    }
}
