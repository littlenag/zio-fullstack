package ziofullstack.backend.service.exceptions

/**
 * Indicates that an error occurred during a database query.
 *
 * @param msg   The error message.
 * @param cause The exception cause.
 */
case class DbException(msg: String, cause: Option[Throwable] = None)
  extends RuntimeException(msg, cause.orNull)
