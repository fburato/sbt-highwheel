lazy val root = (project in file(".")).
  settings(
    name := "sbt-highwheel",
    version := "0.1-SNAPSHOT",
    organization := "org.pitest",
    scalaVersion := "2.12.5",
    sbtPlugin := true,
    sbtVersion := "1.1.1",
    resolver,
    addDependencies
  )//.enablePlugins(org.pitest.highwheel.sbt.AnalyserPlugin)

lazy val resolver = Seq(
  resolvers += Resolver.mavenLocal
)

lazy val dependenciesList = Seq (
  "org.pitest" % "highwheel-modules" % "1.3-SNAPSHOT"
)

lazy val addDependencies = Seq(
  libraryDependencies ++= dependenciesList
)