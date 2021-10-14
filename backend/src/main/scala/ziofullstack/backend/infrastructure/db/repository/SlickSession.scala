package ziofullstack.backend.infrastructure.db.repository

import com.typesafe.config.{Config, ConfigFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcBackend, JdbcProfile}

/**
 * Represents an "open" Slick database and its database (type) profile.
 *
 * <b>NOTE</b>: these databases need to be closed after creation to
 * avoid leaking database resources like active connection pools, etc.
 */
class SlickSession private (val dbConfig: DatabaseConfig[JdbcProfile]) {
  val db: JdbcBackend#Database = dbConfig.db
  val profile: JdbcProfile = dbConfig.profile
}

/**
 * Methods for "opening" Slick databases for use.
 *
 * <b>NOTE</b>: databases created through these methods will need to be
 * closed after creation to avoid leaking database resources like active
 * connection pools, etc.
 */
object SlickSession {

  // TODO make into a layer
  def forConfig(path: String): SlickSession = forConfig(path, ConfigFactory.load())
  def forConfig(config: Config): SlickSession = forConfig("", config)
  def forConfig(path: String, config: Config): SlickSession = forConfig(DatabaseConfig.forConfig[JdbcProfile](path, config))
  def forConfig(databaseConfig: DatabaseConfig[JdbcProfile]): SlickSession = new SlickSession(databaseConfig)
}
