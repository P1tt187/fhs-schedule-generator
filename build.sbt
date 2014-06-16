
name := "schedule-generator"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaJpa,
  javaWs,
  cache,
  "org.webjars" % "bootstrap" % "3.0.0",
  "org.hibernate" % "hibernate-core" % "4.2.3.Final",
  "org.hibernate" % "hibernate-entitymanager" % "4.2.3.Final",
  "mysql" % "mysql-connector-java" % "5.1.21",
  "uk.com.robust-it" % "cloning" % "1.9.0",
  "com.decodified" %% "scala-ssh" % "0.6.4",
  "org.bouncycastle" % "bcprov-jdk16" % "1.46",
  "com.jcraft" % "jzlib" % "1.1.3"
)



scalacOptions ++= Seq("-feature", "-language:postfixOps", "-language:implicitConversions")

lazy val root = (project in file(".")).addPlugins(PlayScala).addPlugins(SbtWeb)


