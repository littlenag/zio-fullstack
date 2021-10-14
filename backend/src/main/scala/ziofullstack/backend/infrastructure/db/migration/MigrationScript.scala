package ziofullstack.backend.infrastructure.db.migration

import zio._
import slick.migration.api.flyway.{MigrationInfo, VersionedMigration}
import slick.migration.api.{Dialect, Migration}

trait MigrationScript {

  implicit val infoProvider: MigrationInfo.Provider[Migration] = MigrationInfo.Provider.compatible

  type Script = ZIO[Has[Dialect[_]], Throwable, Migration]

  def script: Script

  final def versionedMigration: ZIO[Has[Dialect[_]], Throwable, VersionedMigration[Int]] =
    script.map(VersionedMigration(ordinal, _))

  val ordinal: Int = {
    val MigrationClassName = """Migration_(\d+)\$?""".r

    this.getClass.getSimpleName match {
      case MigrationClassName(ordStr) => ordStr.toInt
      case name => throw new IllegalArgumentException(s"Migration class name: $name did not match regex.")
    }
  }
}