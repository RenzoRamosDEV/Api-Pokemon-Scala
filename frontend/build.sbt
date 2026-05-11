name         := "pokedex-play"
organization := "com.pokeapp"
version      := "1.0-SNAPSHOT"
scalaVersion := "3.4.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  guice,
  ws,
  "org.playframework" %% "play-json" % "3.0.4"
)
