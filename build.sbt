
name := """schedule-generator"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaJpa,
  javaWs,
  cache,
  filters,
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.hibernate" % "hibernate-core" % "4.2.3.Final",
  "org.hibernate" % "hibernate-entitymanager" % "4.2.3.Final",
  //"mysql" % "mysql-connector-java" % "5.1.21",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.1.7",
  "uk.com.robust-it" % "cloning" % "1.9.0",
  "com.decodified" %% "scala-ssh" % "0.6.4",
  "org.bouncycastle" % "bcprov-jdk16" % "1.46",
  "com.jcraft" % "jzlib" % "1.1.3"
)

//scalaVersion := "2.11.1"

scalacOptions ++= Seq("-feature", "-language:postfixOps", "-language:implicitConversions")

javaOptions ++=Seq("-Dhttp.port=disabled","-Dhttps.port=9443","-Dhttps.keyStore=/opt/sgd/sslkeystore","-Dhttps.keyStorePassword=sgenerator")


lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtWeb)


