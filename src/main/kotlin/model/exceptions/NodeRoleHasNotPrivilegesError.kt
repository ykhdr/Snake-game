package model.exceptions

class NodeRoleHasNotPrivilegesError(
    message: String? = null,
    cause: Throwable? = null
) : BusinessError(message, cause)