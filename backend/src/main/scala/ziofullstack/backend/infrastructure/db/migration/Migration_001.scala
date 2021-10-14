package ziofullstack.backend.infrastructure.db.migration

import zio._
import slick.migration.api.{Dialect, Migration, TableMigration}
import ziofullstack.backend.infrastructure.db.tables.Tables._

/**
 * Migration v001
 *  - create initial tables, columns, and foreign keys
 */
object Migration_001 extends MigrationScript {

  def script: Script = {
    ZIO.service[Dialect[_]].map { implicit dialect =>
      val users =
        TableMigration(usersTable)
          .create
          .addColumns(_.id, _.email, _.firstName, _.lastName, _.hash, _.activated)

      val authTokens =
        TableMigration(authTokensTable)
          .create
          .addColumns(_.token, _.userId, _.`type`, _.expiry, _.lastTouched)
          .addForeignKeys(_.user)

      users & authTokens
    }
  }
}
