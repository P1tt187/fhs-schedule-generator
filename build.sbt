import com.typesafe.sbt.packager.archetypes.ServerLoader
import com.typesafe.sbt.packager.linux.LinuxSymlink
import com.typesafe.sbt.packager.rpm.RpmDependencies
import com.typesafe.sbt.packager.docker._
import sbt.Keys._
import sbt._
import play.Play.autoImport._
import PlayKeys._

name := """schedule-generator"""

version := "1.2.2"

libraryDependencies ++= Seq(
  javaJdbc,
  javaJpa, //.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
  javaWs,
  cache,
  filters,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.webjars.bower" % "bootstrap-select" % "1.7.3",
  "org.hibernate" % "hibernate-core" % "4.3.10.Final",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.10.Final",
  //"org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final",
  "mysql" % "mysql-connector-java" % "5.1.21",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.1.7",
  "uk.com.robust-it" % "cloning" % "1.9.0",
  "com.decodified" %% "scala-ssh" % "0.7.0",
  "com.typesafe" % "config" % "1.2.0",
  "net.java.dev.jna" % "jna" % "3.4.0"
  //"org.bouncycastle" % "bcprov-jdk16" % "1.46",
  //"com.jcraft" % "jzlib" % "1.1.3"
)

scalaVersion := "2.11.7"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

scalacOptions ++= Seq("-feature", "-language:postfixOps", "-language:implicitConversions")

//javaOptions ++=Seq("-Dhttp.port=disabled","-Dhttps.port=9443","-Dhttps.keyStore=/opt/sgd/sslkeystore","-Dhttps.keyStorePassword=sgenerator")

//scapegoatConsoleOutput := false

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtWeb).enablePlugins(JDKPackagerPlugin).enablePlugins(DockerPlugin).enablePlugins(RpmPlugin).enablePlugins(JDebPackaging).enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "informations"
  )

routesGenerator := InjectedRoutesGenerator

jdkPackagerTool := Some(file("/usr/lib/jvm/default/bin/javapackager"))

maintainer := "Fabian Markert <f.markert87@gmail.com>"

jdkPackagerType := "all"

packageSummary := "This Programm is designed to generate Schedules for Students."

packageDescription := "The main goal is to make it easy to generate a regular Schedule for the Students of faculty informatik from the University of Applied Science Schmalkalden"

rpmRelease := Option(sys.props("rpm.buildNumber")) getOrElse "1"

rpmVendor := "http://www.fh-schmalkalden.de"

rpmUrl := Some("https://github.com/P1tt187/fhs-schedule-generator")

rpmLicense := Some("GPL v3")

serverLoading in Rpm := ServerLoader.Systemd

rpmRequirements+= "mariadb-server"

linuxPackageMappings += {
  val location = target.value
  packageMapping((location, "/usr/share/schedule-generator/")) withUser ("schedule-generator") withGroup ("schedule-generator")
}

dockerCommands+=Cmd("FROM","library/mariadb")

dockerExposedPorts in Docker := Seq(9000, 9443)

PlayKeys.externalizeResources := false

//linuxPackageSymlinks ++= Seq(LinuxSymlink("/usr/share/schedule-generator/RUNNING_PID","/run/schedule-generator/RUNNING_PID"))

