package model.exceptions

open class BusinessError(
    open val details: String? = null,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Error(message, cause)