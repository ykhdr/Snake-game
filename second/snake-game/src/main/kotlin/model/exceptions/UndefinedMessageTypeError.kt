package model.exceptions

class UndefinedMessageTypeError(
    details: String? = null,
    message: String? = null,
    cause: Throwable? = null,
) : BusinessError(details, message, cause)