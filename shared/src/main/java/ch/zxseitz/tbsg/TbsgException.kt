package ch.zxseitz.tbsg;

open class TbsgException(override val message: String,
                         override val cause: Throwable? = null):
    Exception(message, cause)
