package ziofullstack.frontend.util

object ScalaUtil {

  // Avoid warnings for non-unit values that are discarded
  implicit class DiscardHelper[T](val value: T) extends AnyVal {
    def discard: Unit = value match { case _ => () }
  }

}
