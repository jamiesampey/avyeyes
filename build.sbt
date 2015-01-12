organization := "com.avyeyes"

name := "AvyEyes"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.4"

scalacOptions ++= Seq(
  "-target:jvm-1.7", 
  "-encoding", "UTF-8",
  "-unchecked", 
  "-deprecation"
)

// r.js javascript compilation
lazy val rjs = taskKey[Unit]("Runs r.js compilation and optimization")
rjs := {
  import scala.sys.process._
  println("r.js -o build.js".!!)
}

//compile in Compile <<= (compile in Compile) dependsOn(rjs)

// sbt-jasmine config
seq(jasmineSettings : _*)

appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" }

appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" }

jasmineTestDir <+= sourceDirectory { src => src / "test" / "webapp" / "js" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "test.dependencies.js" }

jasmineRequireJsFile <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" / "require" / "require.min.js" }

jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "require.conf.js" }

parallelExecution in Test := false

test in Test <<= (test in Test) dependsOn (jasmine)

// xsbt-web-plugin config
jetty(config = "etc/jetty.xml")

// jar dependencies
libraryDependencies ++= {
  val liftVersion = "2.6"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftweb" %% "lift-testkit" % liftVersion % "compile",
    "org.squeryl" %% "squeryl" % "0.9.6-RC3",
    "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "org.apache.commons" % "commons-lang3" % "3.3.2",
    "com.google.guava" % "guava" % "17.0",
    "org.specs2" %% "specs2" % "2.4.1" % "test",
    "com.h2database" % "h2" % "1.3.176" % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "net.liftmodules" %% ("omniauth_2.6") % "0.15" % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % "9.2.1.v20140609" % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")
  )
}
