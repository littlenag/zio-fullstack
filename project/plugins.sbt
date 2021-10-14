// Code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")

// Revolver allows us to use re-start and work a lot faster!
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// Native Packager allows us to create standalone jar
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")

// ScalaJS and associated plugins
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.7.0")

// Bundle everything using webpack
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.20.0")

// Necessary to build shared JVM/JS libraries.
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

// Extract metadata from sbt and make it available to the code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")

// Automate releases
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")

// Generate Scala bindings from Typescript NPM (https://scalablytyped.org)
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta32")

// Strict scalac options from tpolecat
//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.17")
