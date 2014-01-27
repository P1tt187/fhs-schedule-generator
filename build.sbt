name := "schedule-generator"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  javaWs,
  javaJpa,
  cache,
  "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final"
)

play.Project.playScalaSettings
