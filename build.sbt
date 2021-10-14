ThisBuild / organization := "io.github.littlenag"
ThisBuild / scalaVersion := "2.13.5"

lazy val root: Project = (project in  file("."))
  .configure(Settings.commonSettings)
  .settings(
    name := Settings.name,
    reStart := { (reStart in (backend, Compile)).evaluated },

    Compile / mainClass := Some("ziofullstack.backend.Main")
  )
  //.enablePlugins(DockerPlugin)
  .aggregate(backend,frontend,sharedJVM,sharedJS)
  .dependsOn(backend,frontend,sharedJVM,sharedJS)

lazy val backend = (project in file("backend"))
  .dependsOn(sharedJVM)
  .enablePlugins(SbtWeb, WebScalaJSBundlerPlugin)
  .enablePlugins(BuildInfoPlugin)
  .configure(Settings.commonSettings)
  .settings(
    name := Settings.name + "-backend",

    scalaJSProjects := Seq(frontend,sharedJS),
    Assets / pipelineStages := Seq(scalaJSPipeline),

    // https://github.com/sbt/sbt-web#packaging-and-publishing
    Assets / WebKeys.packagePrefix := "public/",
    Assets / WebKeys.deduplicators += { files: Seq[File] => files.headOption },

    libraryDependencies ++= Settings.backendDependencies.value,

    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,

    // do a fastOptJS on reStart
    reStart := (reStart dependsOn ((fastOptJS / webpack) in (frontend, Compile))).evaluated,

    // This settings makes reStart to rebuild if a scala.js file changes on the client
    watchSources ++= (frontend / watchSources).value,

    // Hook up SBT Web to the resource's in frontend
    //Assets / sourceDirectory := (sourceDirectory in frontend).value / "main" / "assets",
    Assets / resourceDirectory := baseDirectory.value / "public",

    // Support stopping the running server
    reStart / mainClass := Some("ziofullstack.backend.Main"),

    run / fork := true,

    Test / parallelExecution := false,

    buildInfoKeys := Seq[BuildInfoKey](
      "appName" -> Settings.name,
      name,
      version,
      scalaVersion,
      sbtVersion,
      BuildInfoKey.action("buildTime") { System.currentTimeMillis },
      scalaJSStage
    ),
    buildInfoPackage := "ziofullstack.misc"
  )

///

lazy val frontend = (project in file("frontend"))
  .dependsOn(sharedJS)
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSWeb)
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalablyTypedConverterPlugin)
  .configure(Settings.commonSettings, bundlerSettings)
  .settings(
    name := Settings.name + "-frontend",

    // use Scala.js provided launcher code to start the client app
    Compile / mainClass := Some("ziofullstack.frontend.Main"),
    scalaJSUseMainModuleInitializer := true,

    libraryDependencies ++= Settings.frontendDeps.value,

    Compile / npmDependencies ++= Settings.frontendNpmDeps,
    Compile / npmDevDependencies ++= Settings.npmDevDeps,

    // #316
    stTypescriptVersion := "4.2.4",

    stFlavour := Flavour.Japgolly,

    // Still want: react
    stIgnore ++= List(
      "react-dom",
      "bootstrap",
      "jquery",
      "popper.js",
      "react-bootstrap",
      "prop-types",
      "csstype",
      "@fortawesome/fontawesome-free",
      "core-js",
      "styled-components",
      "ajv",
      "mobx",
      "mobx-react"
    ),

    webpackEmitSourceMaps := true,

    webpackBundlingMode := BundlingMode.Application,

    // Use a custom config file to export the JS dependencies to the global namespace,
    // as expected by the scalajs-react facade
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.webpack.config.js"),
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "prod.webpack.config.js"),

    // Uniformises fastOptJS/fullOptJS output file name
    artifactPath in(Compile, fastOptJS) := ((crossTarget in(Compile, fastOptJS)).value / "frontend.js"),
    artifactPath in(Test, fastOptJS) := ((crossTarget in(Test, fastOptJS)).value / "frontend.test.js"),
    artifactPath in(Compile, fullOptJS) := ((crossTarget in(Compile, fullOptJS)).value / "frontend.js"),
    artifactPath in(Test, fullOptJS) := ((crossTarget in(Test, fullOptJS)).value / "frontend.test.js")
  )

// Shared definitions shared between Backend and Frontend
lazy val shared =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("shared"))
    .configure(Settings.commonSettings)
    .settings(
      name := Settings.name + "-shared",

      // Disable coverage until the ScalaJS support is better
      coverageEnabled := false,

      webpackEmitSourceMaps := true,

      libraryDependencies ++= Settings.sharedDependencies.value,

      // Uniformises fastOptJS/fullOptJS output file name
      artifactPath in(Compile, fastOptJS) := ((crossTarget in(Compile, fastOptJS)).value / "shared.js"),
      artifactPath in(Test, fastOptJS) := ((crossTarget in(Test, fastOptJS)).value / "shared.test.js"),
      artifactPath in(Compile, fullOptJS) := ((crossTarget in(Compile, fullOptJS)).value / "shared.js"),
      artifactPath in(Test, fullOptJS) := ((crossTarget in(Test, fullOptJS)).value / "shared.test.js")
    )
    // beware enabling any plugins that themselves enable ScalaJS! this tends to break how deps are resolved!

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js.enablePlugins(ScalaJSBundlerPlugin).enablePlugins(ScalaJSWeb)

lazy val bundlerSettings: Project => Project =
  _.settings(
    Compile / fastOptJS / webpackExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackExtraArgs += "--mode=production",
    Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production"
  )
