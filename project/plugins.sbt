
// Comment to get more information during initialization
logLevel := Level.Warn

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "spray repo" at "http://repo.spray.io",
  "jitpack.io" at "https://jitpack.io/",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)


// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.1")

//addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "0.94.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")