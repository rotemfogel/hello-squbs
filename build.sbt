
name := "squbs-hello"

version := "0.1.0-SNAPSHOT"

organization in ThisBuild := "me.rotemfo"

scalaVersion := "2.12.8"

crossPaths := false

resolvers += Resolver.sonatypeRepo("snapshots")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

import Versions._

Revolver.settings

libraryDependencies ++= Seq(
  "ch.qos.logback"     % "logback-classic"     % logbackVer,
  "org.squbs"         %% "squbs-unicomplex"    % squbsVer,
  "org.squbs"         %% "squbs-actormonitor"  % squbsVer,
  "org.squbs"         %% "squbs-actorregistry" % squbsVer,
  "org.squbs"         %% "squbs-httpclient"    % squbsVer,
  "org.squbs"         %% "squbs-admin"         % squbsVer,
  "org.json4s"        %% "json4s-native"       % json4sVer,
  "de.heikoseeberger" %% "akka-http-json4s"    % akkaHttpJson4sVer,
  "org.squbs"         %% "squbs-testkit"       % squbsVer           % Test,
  "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVer        % Test
)

mainClass in (Compile, run) := Some("org.squbs.unicomplex.Bootstrap")

enablePlugins(PackPlugin)

packMain := Map("run" -> "org.squbs.unicomplex.Bootstrap")

enablePlugins(DockerPlugin)

dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = "org.squbs.unicomplex.Bootstrap"
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName)
    .mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("java")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}