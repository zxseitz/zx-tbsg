package ch.zxseitz.tbsg.server.model.request;

interface IRequest {
    fun validate(): Boolean
}
