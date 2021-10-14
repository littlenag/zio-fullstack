package ziofullstack.backend

import zio._
import zio.logging._
import ziofullstack.shared.domain.Env
/**
  *
  */
package object util {
  implicit class AnyEx[T](val v: T) extends AnyVal {
    def |>[U](f: T => U): U = f(v)
  }

  implicit class ZLayerHasEx[I,E,O: Tag](layer: ZLayer[I,E,Has[O]]) {
    def get: ZLayer[I,E,O] = layer.map(_.get[O])
  }

  def inferEnv: ZIO[system.System, Throwable, Env] = for {
    maybeProp <- zio.system.property("env")
    maybeEnv <- zio.system.env("ENV")
    maybeName = maybeProp orElse maybeEnv
  } yield maybeName.flatMap(Env.withNameInsensitiveOption) getOrElse Env.Dev

  type ZLogger = Logger[String]

  type TaskE[E,A] = Task[Either[E,A]]
}
