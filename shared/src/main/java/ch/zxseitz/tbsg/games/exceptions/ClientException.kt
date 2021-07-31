package ch.zxseitz.tbsg.games.exceptions;

import ch.zxseitz.tbsg.TbsgException;

class ClientException(override val message: String,
                      override val cause: Throwable? = null):
    TbsgException(message, cause)
