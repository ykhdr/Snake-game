package model.exceptions

open class BusinessError(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Error(message, cause)