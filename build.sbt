import sbt._
import Keys._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

name := """schedule-generator"""

version := "1.0"

libraryDependencies ++= Seq(
  javaJdbc,
  javaJpa.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
  javaWs,
  cache,
  filters,
  "org.webjars" %% "webjars-play" % "2.3.0-3",
  "org.webjars" % "bootstrap" % "3.3.2",
  "org.hibernate" % "hibernate-core" % "4.3.8.Final",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.8.Final",
  //"org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final",
  "mysql" % "mysql-connector-java" % "5.1.21",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.1.7",
  "uk.com.robust-it" % "cloning" % "1.9.0",
  "com.decodified" %% "scala-ssh" % "0.7.0"
  //"org.bouncycastle" % "bcprov-jdk16" % "1.46",
  //"com.jcraft" % "jzlib" % "1.1.3"
)

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-feature", "-language:postfixOps", "-language:implicitConversions")

//javaOptions ++=Seq("-Dhttp.port=disabled","-Dhttps.port=9443","-Dhttps.keyStore=/opt/sgd/sslkeystore","-Dhttps.keyStorePassword=sgenerator")

scapegoatConsoleOutput := false

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtWeb)


maintainer in Linux := "Fabian Markert <f.markert87@gmail.com>"

packageSummary in Linux := "This Programm is designed to generate Schedules for Students."

packageDescription := "The main goal is to make it easy to generate a regular Schedule for the Students of faculty informatik from the University of Applied Science Schmalkalden"

rpmRelease := "1"

rpmVendor := "http://www.fh-schmalkalden.de"

rpmUrl := Some("https://github.com/P1tt187/fhs-schedule-generator")

rpmLicense := Some("GPL v3")



