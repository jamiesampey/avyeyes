organization := "com.avyeyes"

name := "AvyEyes"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.5"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked", 
  "-deprecation"
)

import NativePackagerKeys._

enablePlugins(JettyPlugin, JavaAppPackaging)

lazy val rjs = taskKey[Unit]("Runs r.js javascript compilation")

rjs := {
  import scala.sys.process._
  println("r.js -o build.js".!!)
}

packageBin in Compile <<= (packageBin in Compile) dependsOn rjs

// sbt-jasmine config
seq(jasmineSettings : _*)

appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" }

appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" }

jasmineTestDir <+= sourceDirectory { src => src / "test" / "webapp" / "js" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "test.dependencies.js" }

jasmineRequireJsFile <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" / "require.min.js" }

jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "require.conf.js" }

jasmineEdition := 2

parallelExecution in Test := false

test in Test <<= (test in Test) dependsOn (jasmine)


javaOptions in Jetty ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8788"
)

containerConfigFile := Some(file("etc/jetty.xml"))


// jar dependencies
libraryDependencies ++= {
  val liftVersion = "2.6.2"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftweb" %% "lift-testkit" % liftVersion % "compile",
    "net.liftmodules" %% "omniauth_2.6" % "0.17" % "compile",
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "com.typesafe.slick" %% "slick" % "3.0.0",
    "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.10.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.apache.commons" % "commons-lang3" % "3.3.2",
    "com.google.guava" % "guava" % "17.0",

    // test dependencies
    "org.specs2" %% "specs2" % "2.4.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
    "com.h2database" % "h2" % "1.3.176" % "test",
    "org.eclipse.jetty" % "jetty-webapp" % "9.3.2.v20150730" % "test"
  )
}
