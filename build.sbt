ThisBuild / organization := "com.pokeapp"
ThisBuild / scalaVersion := "3.4.3"
ThisBuild / version      := "0.1.0-SNAPSHOT"

val http4sVersion      = "0.23.27"
val circeVersion       = "0.14.10"
val catsEffectVersion  = "3.5.4"
val log4catsVersion    = "2.6.0"
val pureConfigVersion  = "0.17.7"
val scaffeineVersion   = "5.3.0"
val munitVersion       = "1.0.3"
val munitCEVersion     = "2.0.0"
val prometheusVersion  = "0.24.0"
val logbackVersion     = "1.5.13"


lazy val root = (project in file("."))
  .settings(
    name := "pokeapi-scala",
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Wconf:cat=deprecation:s",
      "-feature",
      "-unchecked",
      "-Wunused:all"
    ),
    libraryDependencies ++= Seq(
      // HTTP server + client
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,

      // Prometheus metrics
      "org.http4s" %% "http4s-prometheus-metrics" % prometheusVersion,



      // JSON
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,

      // Config
      "com.github.pureconfig" %% "pureconfig-core"        % pureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,

      // Cache
      "com.github.blemale" %% "scaffeine" % scaffeineVersion,

      // Logging
      "org.typelevel"   %% "log4cats-core"   % log4catsVersion,
      "org.typelevel"   %% "log4cats-slf4j"  % log4catsVersion,
      "ch.qos.logback"  %  "logback-classic" % logbackVersion,

      // Testing
      "org.scalameta" %% "munit"                   % munitVersion   % Test,
      "org.typelevel" %% "munit-cats-effect"        % munitCEVersion % Test,
      "org.http4s"    %% "http4s-client"            % http4sVersion  % Test,
    ),

    // Assembly
    assembly / assemblyJarName := "pokeapi-assembly.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", _*) => MergeStrategy.concat
      case PathList("META-INF", _*)             => MergeStrategy.discard
      case PathList("reference.conf")           => MergeStrategy.concat
      case _                                    => MergeStrategy.first
    },

    // Coverage
    coverageMinimumStmtTotal := 80,
    coverageFailOnMinimum    := false,

    testFrameworks += new TestFramework("munit.Framework")
  )
