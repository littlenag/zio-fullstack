package ziofullstack.backend.infrastructure.db.tables

import zio._
import slick.dbio.DBIO
import slick.lifted.{CanBeQueryCondition, Query, Rep}
import ziofullstack.backend.infrastructure.db.repository.SlickSession

import scala.concurrent.ExecutionContext

/**
  * Methods to help lift slick results into cats effects.
  */
object ZioInterop {

  implicit class DBIOOps[A](val dbio: DBIO[A]) extends AnyVal {

    def toZIO: ZIO[Has[SlickSession], Throwable, A] =
      ZIO.serviceWith[SlickSession] { session =>
        ZIO.fromFuture(_ => session.db.run(dbio))
      }
  }

  implicit class ZIOObjOps(private val obj: ZIO.type) extends AnyVal {
    def fromDBIO[R](f: ExecutionContext => DBIO[R]): ZIO[Has[SlickSession], Throwable, R] =
      for {
        db <- ZIO.access[Has[SlickSession]](_.get.db)
        r <- ZIO.fromFuture(ec => db.run(f(ec)))
      } yield r

    def fromDBIO_[R](f: ExecutionContext => DBIO[R])(implicit session: SlickSession): ZIO[Any, Throwable, R] =
      for {
        r <- ZIO.fromFuture(ec => session.db.run(f(ec)))
      } yield r
  }

  implicit class ConditionalQueryFilter[A, B, C[_]](q: Query[A, B, C]) {
    def filterOpt[D, T <: Rep[_]: CanBeQueryCondition](
      option: Option[D]
    )(f: (A, D) => T): Query[A, B, C] =
      option.map(d => q.filter(a => f(a, d))).getOrElse(q)

    def filterIf(p: Boolean)(f: A => Rep[Boolean]): Query[A, B, C] =
      if (p) q.filter(f) else q
  }
}