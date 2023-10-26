package model.exceptions

class UndefinedMessageTypeError(
    message: String? = null,
    cause: Throwable? = null,
) : BusinessError(message, cause)