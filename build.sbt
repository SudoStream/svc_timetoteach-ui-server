import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerRepository, dockerUpdateLatest}

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)
enablePlugins(DockerPlugin)

name := "timetoteach"

val scalaV = "2.11.11"

lazy val timetoteach_ui_server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  version := "0.0.1",
  dockerBaseImage := "anapsix/alpine-java:8_server-jre",
  dockerExposedPorts := Seq(9000),
  dockerRepository := Some("eu.gcr.io/time-to-teach"),
  dockerUpdateLatest := true,
  //version in Docker := version.value + "-" + java.util.UUID.randomUUID.toString
  packageName in Docker := "time-to-teach/api",
  resolvers += Resolver.sonatypeRepo("snapshots"),

// triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    guice,
    ws,
    "io.sudostream.timetoteach" %% "messages" % "0.0.11-14",
    "com.google.api-client" % "google-api-client" % "1.22.0",
    "be.objectify" %% "deadbolt-scala" % "2.6.0",
    "com.vmunier" %% "scalajs-scripts" % "1.1.0",
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
    "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0"
  ),
  jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % "15.6.1"
      / "react-with-addons.js"
      minified "react-with-addons.min.js"
      commonJSName "React",

    "org.webjars.bower" % "react" % "15.6.1"
      / "react-dom.js"
      minified "react-dom.min.js"
      dependsOn "react-with-addons.js"
      commonJSName "ReactDOM",

    "org.webjars.bower" % "react" % "15.6.1"
      / "react-dom-server.js"
      minified "react-dom-server.min.js"
      dependsOn "react-dom.js"
      commonJSName "ReactDOMServer")
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  enablePlugins(ScalaJSPlugin).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project timetoteach_ui_server", _: State)) compose (onLoad in Global).value
