package model.exceptions

class StateEditError(
    message: String? = null,
    cause: Throwable? = null
) : BusinessError(message, cause)