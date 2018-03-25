lazy val root = (project in file("."))
    .settings(
    name := "sbt-highwheel",
    version := "0.1-SNAPSHOT",
    organization := "com.github.fburato",
    scalaVersion := "2.12.5",
    sbtPlugin := true,
    sbtVersion := "1.1.1",
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
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  // License of your choice
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

  // or if you want to set these fields manually
  homepage := Some(url("https://github.com/fburato/sbt-highwheel")),
  scmInfo := Some(
  ScmInfo(
    url("https://github.com/fburato/sbt-highwheel"),
    "scm:git@github.com:fburato/sbt-highwheel.git"
  )),
  developers := List(
  Developer(id="fburato", name="Francesco Burato", email="frankburato-github@yahoo.com", url=url("https://github.com/fburato"))
  ),
  publishArtifact in Test := false
)

lazy val resolver = Seq(
  resolvers += Resolver.mavenLocal,
  resolvers +=
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
)

lazy val dependenciesList = Seq (
  "com.github.fburato" % "highwheel-modules" % "2.0"
)

lazy val addDependencies = Seq(
  libraryDependencies ++= dependenciesList
)