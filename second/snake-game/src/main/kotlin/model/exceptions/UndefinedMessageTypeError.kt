package model.exceptions

class UndefinedMessageTypeError(
    override val details: String? = null,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : BusinessError(details, message, cause)