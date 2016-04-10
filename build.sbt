name := """openrecipes"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

EclipseKeys.preTasks := Seq(compile in Compile)

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  evolutions,
  "org.jsoup" % "jsoup" % "1.8.3"
)
