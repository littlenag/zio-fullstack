package ziofullstack.frontend.util

import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.|.Evidence

/**
  *
  */
object JsUtil {

  /**
    * Widen a js.Array taking '|' types into account.
    * @param arr
    * @tparam T
    */
  implicit class ArrayOps[T](val arr: js.Array[T]) extends AnyVal {
    final def widen[W](implicit ev: Evidence[T, W]): js.Array[W] =
      arr.asInstanceOf[js.Array[W]]
  }

  /**
    * Helper for Null types in |
    * @tparam T
    */
  implicit class RightNullOps[T, S](val tOrS: T | S) extends AnyVal {
    final def optionT(implicit ev: S =:= Null): Option[T] =
      Option(tOrS.asInstanceOf[T])
    final def rOptionT(implicit ev: T =:= Null): Option[S] =
      Option(tOrS.asInstanceOf[S])
  }

}
