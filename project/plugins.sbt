
// Comment to get more information during initialization
logLevel := Level.Warn

//lazy val root = Project("plugins", file(".")).dependsOn(plugin)

//lazy val plugin = file("../").getCanonicalFile.toURI

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "spray repo" at "http://repo.spray.io"
)


// Use the Play sbt plugin for Play projects
//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3-SNAPSHOT")



//addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0-M2b")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.7")

