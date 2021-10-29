import ReleaseTransformations._
import sbtrelease.ReleaseStateTransformations.runClean
lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-highwheel",
    organization := "com.github.fburato",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8" // set minimum sbt version
        case "2.13" => "1.4.5"
      }
    },
    resolver,
    addDependencies,
    release
  )

lazy val release = Seq(
  // To sync with Maven central, you need to supply the following information:
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/fburato/sbt-highwheel")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/fburato/sbt-highwheel"),
      "scm:git@github.com:fburato/sbt-highwheel.git"
    )
  ),
  developers := List(
    Developer(
      id = "fburato",
      name = "Francesco Burato",
      email = "francesco.burato@gmail.com",
      url = url("https://github.com/fburato")
    )
  ),
  Test / publishArtifact := false,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishM2"),
    releaseStepCommand("publishSigned"),
    releaseStepCommand("sonatypeRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges,
    runClean,
    runTest,
    releaseStepCommand("publishM2"),
    releaseStepCommand("publishSigned"),
    pushChanges
  )
)

lazy val resolver = Seq(
  resolvers += Resolver.mavenLocal,
  resolvers +=
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
)

lazy val dependenciesList = Seq("com.github.fburato" %% "highwheel-modules-core" % "2.1.5")

lazy val addDependencies = Seq(libraryDependencies ++= dependenciesList)
