name := "schedule-generator"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaWs,
  javaJpa,
  cache,
  "org.hibernate" % "hibernate-core" % "4.2.3.Final",
  "org.hibernate" % "hibernate-entitymanager" % "4.2.3.Final",
  "mysql" % "mysql-connector-java" % "5.1.21",
  "uk.com.robust-it" % "cloning" % "1.9.0"
)

scalacOptions ++= Seq("-feature", "-language:postfixOps", "-language:implicitConversions")

play.Project.playScalaSettings
