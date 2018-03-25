resolvers += Resolver.mavenLocal
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")