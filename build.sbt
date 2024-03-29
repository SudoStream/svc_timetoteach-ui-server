import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerRepository, dockerUpdateLatest}
import sbt.Keys.libraryDependencies

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)
enablePlugins(DockerPlugin)

name := "timetoteach"

val scalaV = "2.11.11"

///////////////////////////////////////////
val WWW_TIMETOTEACH_VERSION = "0.0.1-488"
///////////////////////////////////////////

lazy val timetoteach_ui_server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  version := WWW_TIMETOTEACH_VERSION,
  dockerBaseImage := "anapsix/alpine-java:8u144b01_server-jre",
  dockerExposedPorts := Seq(9000),
  dockerRepository := Some("eu.gcr.io/time-to-teach-zone"),
  dockerUpdateLatest := true,
  dockerEntrypoint := Seq("sh", "-c", s"bin/${executableScriptName.value}"),
  packageName in Docker := "www-time-to-teach",
  resolvers += Resolver.sonatypeRepo("snapshots"),

  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    guice,
    ws,
    "io.sudostream.timetoteach" %% "messages" % "0.0.11-57",
    "com.google.api-client" % "google-api-client" % "1.22.0",
    "be.objectify" %% "deadbolt-scala" % "2.6.0",
    "com.vmunier" %% "scalajs-scripts" % "1.1.0",
    "com.typesafe.play" %% "play-mailer" % "6.0.1",
    "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
    "com.lihaoyi" %% "upickle" % "0.5.1",
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0",
    "io.netty" % "netty-all" % "4.1.15.Final",
    "com.builtamont" %% "play2-scala-pdf" % "2.0.0.P26",
    "net.codingwell" %% "scala-guice" % "4.1.0",

    //test
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.2",
    "com.lihaoyi" %%% "scalatags" % "0.6.7",
    "org.scalactic" %% "scalactic" % "3.0.4",
    "com.lihaoyi" %%% "upickle" % "0.5.1",
    //test
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    specs2 % Test
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
      "org.scala-js" %%% "scalajs-java-time" % "0.2.2",
      specs2 % Test
    )
  ).
  enablePlugins(ScalaJSPlugin).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

//lazy val mainProject =
//  (project in file("."))
//    .aggregate(client, timetoteach_ui_server, shared.jvm)

// loads the server project at sbt startup
//onLoad in Global := mainProject compose (onLoad in Global).value
onLoad in Global := (Command.process("project timetoteach_ui_server", _: State)) compose (onLoad in Global).value

scalacOptions ++= Seq("-feature")
