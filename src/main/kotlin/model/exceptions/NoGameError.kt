package model.exceptions

class NoGameError (
    message: String? = null,
    cause: Throwable? = null
) : BusinessError(message, cause)