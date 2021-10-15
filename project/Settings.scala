import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport._

/**
  * Application settings. Configure the build for your application here.
  * You normally don't have to touch the actual build definition after this.
  */
object Settings {

  /** The name of the application */
  val name = "zio-fullstack"

  lazy val commonSettings: Project => Project = _.settings(
    Rpm / publish := {},
    resolvers ++= Seq(
      Resolver.sonatypeRepo("public"),
      Resolver.sonatypeRepo("snapshots")
    ),
    scalacOptions += "-Ymacro-annotations",
    addCompilerPlugin(
      "org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full
    )
  )

  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
    val `cats` = "2.6.0"
    val `circe` = "0.13.0"
    val `circe-config` = "0.7.0"
    val `enumeratum-circe` = "1.6.1"
    val `enumeratum-slick` = "1.6.0"
    val `h2` = "1.4.197"
    val `http4s` = "0.23.1"
    val `logback` = "1.2.3"
    val `log4Cats` = "2.1.1"
    val `scalatest` = "3.2.9"
    val `flyway` = "5.2.4"
    val `tsec` = "0.4.0"
    val `courier` = "2.0.0"
    val `refined` = "0.9.25"

    val `commons-validator` = "1.6"

    val scalaTags = "0.9.4"
    val scalaDom = "1.1.0"

    val scalajsReactFacade = "1.7.7"
    val diode = "1.1.14"
    val diodeReact = "1.1.14"

    val scalajsReactBridge = "0.8.5"

    // Material-UI
    val scalajsReactComponents = "1.0.0-M2"

    // react-bootstrap
    val scalajsReactBootstrap = "0.1.0"

    // react-clipboard
    val reactClipBoard = "0.10.6"

    val `scalaCSS` = "0.7.0"

    val `bootstrapFacade` = "2.3.5"
  }

  import versions._

  /**
    * Dependencies only used by the Server.
    */
  val backendDependencies = Def.setting(
    Seq(
      // Core deps
      "org.typelevel" %% "cats-core" % `cats`,
      "io.circe" %% "circe-generic" % `circe`,
      "io.circe" %% "circe-literal" % `circe`,
      "io.circe" %% "circe-generic-extras" % `circe`,
      "io.circe" %% "circe-parser" % `circe`,
      "io.circe" %% "circe-refined" % `circe`,
      //"io.circe" %% "circe-config" % `circe-config`,
      "com.beachape" %% "enumeratum-circe" % `enumeratum-circe`,
      "com.beachape" %% "enumeratum-slick" % `enumeratum-slick`,
      "org.http4s" %% "http4s-blaze-server" % `http4s`,
      //"org.http4s" %% "http4s-blaze-client" % `http4s`,
      "org.http4s" %% "http4s-circe" % `http4s`,
      "org.http4s" %% "http4s-dsl" % `http4s`,
      //
      "org.typelevel" %% "log4cats-core" % `log4Cats`,
      "org.typelevel" %% "log4cats-slf4j" % `log4Cats`,
      "ch.qos.logback" % "logback-classic" % `logback`,
      // ZIO
      "dev.zio" %% "zio" % "1.0.11",
      "dev.zio" %% "zio-macros" % "1.0.11",
      "dev.zio" %% "zio-prelude" % "1.0.0-RC6",
      "dev.zio" %% "zio-interop-cats" % "3.1.1.0",
      "dev.zio" %% "zio-logging" % "0.5.11",
      "dev.zio" %% "zio-logging-slf4j" % "0.5.11",
      "dev.zio" %% "zio-config" % "1.0.10",
      "dev.zio" %% "zio-config-magnolia" % "1.0.10",
      "dev.zio" %% "zio-config-typesafe" % "1.0.10",
      "io.github.kitlangton" %% "zio-magic" % "0.3.8",
      // For sending activation and recovery emails
      "com.github.daddykotex" %% "courier" % `courier`,
      "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test",
      "dnsjava" % "dnsjava" % "2.1.9",
      "commons-validator" % "commons-validator" % `commons-validator`,
      "eu.timepit" %% "refined" % `refined`,
      "eu.timepit" %% "refined-cats" % `refined`,
      "eu.timepit" %% "refined-eval" % `refined`,
      "eu.timepit" %% "refined-pureconfig" % `refined`,
      "eu.timepit" %% "refined-scalacheck" % `refined` % "test",
      "eu.timepit" %% "refined-shapeless" % `refined`,
      // Authentication dependencies
      "io.github.jmcardon" %% "tsec-common" % `tsec`,
      "io.github.jmcardon" %% "tsec-password" % `tsec`,
      "io.github.jmcardon" %% "tsec-mac" % `tsec`,
      "io.github.jmcardon" %% "tsec-signatures" % `tsec`,
      "io.github.jmcardon" %% "tsec-jwt-mac" % `tsec`,
      "io.github.jmcardon" %% "tsec-jwt-sig" % `tsec`,
      "io.github.jmcardon" %% "tsec-http4s" % `tsec`,
      // Database dependencies
      "com.rms.miu" %% "slick-cats" % "0.10.4", // https://github.com/RMSone/slick-cats
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "com.zaxxer" % "HikariCP" % "3.4.5",
      "org.postgresql" % "postgresql" % "42.2.2",
      "org.flywaydb" % "flyway-core" % `flyway`,
      "io.github.nafg.slick-migration-api" %% "slick-migration-api" % "0.8.2",
      "io.github.nafg.slick-migration-api" %% "slick-migration-api-flyway" % "0.8.1",
      "com.amazonaws" % "aws-java-sdk" % "1.11.1015",
      // Test dependencies
      "org.scalatest" %% "scalatest" % `scalatest` % Test
    )
  )

  /**
    * These dependencies are shared between JS and JVM projects
    * the special %%% function selects the correct version for each project
    */
  val sharedDependencies = Def.setting(
    Seq(
      // ZIO
      "dev.zio" %%% "zio" % "1.0.11",
      "dev.zio" %%% "zio-macros" % "1.0.11",
      "dev.zio" %%% "zio-interop-cats" % "3.1.1.0",

      // Other
      "com.lihaoyi" %%% "scalatags" % scalaTags,
      "com.beachape" %%% "enumeratum-circe" % `enumeratum-circe`,
      "io.circe" %%% "circe-generic" % `circe`,
      "io.circe" %%% "circe-literal" % `circe`,
      "io.circe" %%% "circe-generic-extras" % `circe`,
      "io.circe" %%% "circe-parser" % `circe`,
      // Automagic RPC using Sloth, Boopickle/Circe to handle wire encoding
      "com.github.cornerman" %%% "sloth"     % "0.3.0",
      "io.suzaku"            %%% "boopickle" % "1.4.0",
      // Search deps
      "org.typelevel" %%% "cats-core" % `cats`,
      // Test deps
      "org.scalatest" %%% "scalatest" % `scalatest` % Test
    )
  )

  /** Dependencies only used by the Frontend ScalaJS code base (note the use of %%% instead of %%) */
  val frontendDeps = Def.setting(
    Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % scalajsReactFacade, // withSources (),
      "com.github.japgolly.scalajs-react" %%% "extra" % scalajsReactFacade,
      "com.github.japgolly.scalajs-react" %%% "ext-cats" % scalajsReactFacade,
      "com.payalabs" %%% "scalajs-react-bridge" % scalajsReactBridge,
      "com.github.japgolly.scalacss" %%% "core" % `scalaCSS`,
      "com.github.japgolly.scalacss" %%% "ext-react" % `scalaCSS`,
      "io.suzaku" %%% "diode" % diode,
      "io.suzaku" %%% "diode-react" % diodeReact,
      "org.scala-js" %%% "scalajs-dom" % scalaDom,
      "io.lemonlabs" %%% "scala-uri" % "3.2.0",
      // Facades of other JavaScript libraries
      "io.github.littlenag" %%% "scalajs-react-bootstrap" % scalajsReactBootstrap,
      "io.7mind.izumi" %%% "logstage-core" % "1.0.6",
      "org.scalatest" %%% "scalatest" % `scalatest` % Test
    )
  )

  val reactVersion = "16.8.4"

  /**
    * eventually migrate to:
    * @stripe/react-stripe-js @stripe/stripe-js
    */
  val frontendNpmDeps = Seq(
    "@fortawesome/fontawesome-free" -> "5.7.2",
    "react-bootstrap" -> "1.0.0-beta.5",
    "bootstrap" -> "4.1.1",
    "jquery" -> "3.2.1",
    "popper.js" -> "1.14.6",
    "ajv" -> "6.9.1",
    "@types/react" -> reactVersion,
    "react" -> reactVersion,
    "react-dom" -> reactVersion,
    "core-js" -> "3.1.4",
    "mobx" -> "5.15.0",
    "mobx-react" -> "6.1.4",
    "styled-components" -> "4.1.1"
  )

  // Share dev deps between all ScalaJS projects
  val npmDevDeps = Seq(
    // Webpack Loaders for CSS and more
    "webpack-merge" -> "4.2.2",
    "css-loader" -> "3.4.2",
    "style-loader" -> "1.1.3",
    "file-loader" -> "5.1.0",
    "url-loader" -> "3.0.0",
    "postcss-loader" -> "^2.1.1",
    "precss" -> "^3.1.2"
    //"node-sass" -> "4.12.0",
    //"sass-loader" -> "6.0.7"
  )
}
