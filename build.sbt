import sbt._
import sbt.Keys._

lazy val rootDependencies = {
  val circeVersion = "0.7.0"
  Seq(
    "de.heikoseeberger" %% "akka-http-circe" % "1.12.0"
    , "com.typesafe.akka" %% "akka-http" % "10.0.3"
    , "io.circe" %% "circe-core" % circeVersion
    , "io.circe" %% "circe-generic" % circeVersion
    , "io.circe" %% "circe-parser" % circeVersion
    , "io.circe" %% "circe-optics" % circeVersion
    // logging
    , "com.typesafe.akka" %% "akka-slf4j" % "2.4.17"
    , "org.slf4j" % "slf4j-api" % "1.7.22"
    , "ch.qos.logback" % "logback-classic" % "1.2.1"
    // others
    , "io.github.todokr" %% "emojipolation" % "0.1.0"
  )
}

lazy val testDependencies = {
  Seq(
    "org.scalactic" %% "scalactic" % "3.0.1"
    , "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ,"com.typesafe.akka" %% "akka-http-testkit" % "10.0.3" % "test"
  )
}

lazy val dependencies = rootDependencies ++ testDependencies

lazy val compileSettings = Seq(
  "-deprecation"
  ,"-target:jvm-1.8"
)

lazy val forkedJvmOption = Seq(
  "-server"
)

lazy val settings = Seq(
  name := "Earthquake Echo skill"
  , organization := "com.microworkflow"
  , version := "0.0.2-SNAPSHOT"
  , scalaVersion := "2.12.1"
  , libraryDependencies ++= dependencies
  , fork in run := true
  , fork in Test := true
  , fork in testOnly := true
  , connectInput in run := true
  , javaOptions in run ++= forkedJvmOption
  , javaOptions in Test ++= forkedJvmOption
  , scalacOptions := compileSettings
)

lazy val buildinfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
  , buildInfoOptions += BuildInfoOption.ToJson
)

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("java")
    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
    copy(appDir, targetDir)
    expose(8080)
  }
}

val main =
  project
    .in(file("."))
    .enablePlugins(BuildInfoPlugin,JavaAppPackaging,sbtdocker.DockerPlugin)
    .settings(buildinfoSettings ++ settings: _*)
