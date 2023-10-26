package model.exceptions

class NodeError(
    message: String? = null,
    cause: Throwable? = null
) : BusinessError(message, cause)