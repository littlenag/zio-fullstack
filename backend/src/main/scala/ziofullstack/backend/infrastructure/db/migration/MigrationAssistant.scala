package ziofullstack.backend.infrastructure.db.migration

import zio._
import slick.migration.api.flyway._
import slick.migration.api.GenericDialect

import ziofullstack.backend.infrastructure.db.repository.SlickSession

import scala.languageFeature.implicitConversions

trait MigrationAssistant {
  def migrate(): Task[Unit]
  def clean(): Task[Unit]
  def validate(): Task[Unit]
}

/**
  * Wraps Flyway to perform the actual migrations.
  *
  * Flyway is set to use the slick-migration-api DSL for the underlying transformations.
  *
  * Actual Migration logic is co-located.
  */
object MigrationAssistant {

  val defaultMigrationScripts =
    Seq(
      Migration_001
    )

  def live(scripts: Seq[MigrationScript] = defaultMigrationScripts): ZIO[Has[SlickSession], Throwable, MigrationAssistant] = {
    for {
      session <- ZIO.service[SlickSession]

      dialect = GenericDialect(session.profile)

      migrations <- ZIO.foreach(scripts) { script =>
        script.versionedMigration.provide(Has(dialect))
      }

      flyway <- Task(SlickFlyway(session.db)(migrations).load())

    } yield {
      new MigrationAssistant {
        override def migrate(): Task[Unit] = Task(flyway.migrate())

        override def clean(): Task[Unit] = Task(flyway.clean())

        override def validate(): Task[Unit] = Task(flyway.validate())
      }
    }
  }
}