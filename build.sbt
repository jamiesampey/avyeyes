organization := "com.avyeyes"

name := "AvyEyes"

version := "0.4.6"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation"
)

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= {
  val liftVersion = "2.6.2"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion,
    "net.liftweb" %% "lift-testkit" % liftVersion,
    "net.liftmodules" %% "omniauth_2.6" % "0.17",
    "com.typesafe.akka" %% "akka-actor" % "2.3.13",
    "com.typesafe.slick" %% "slick" % "3.0.3",
    "org.postgresql" % "postgresql" % "9.4-1202-jdbc41",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.10.15",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "org.apache.commons" % "commons-lang3" % "3.4",
    "com.google.guava" % "guava" % "18.0",
    "org.specs2" %% "specs2" % "2.4.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
    "com.h2database" % "h2" % "1.4.188" % "test",
    "javax.servlet" % "servlet-api" % "2.5" % "test"
  )
}

lazy val mode = taskKey[String]("Build mode (dev or prod)")

mode := sys.props.getOrElse("mode", default = "dev")

webappPostProcess := { origWebapp =>
  if (mode.value == "prod") {
    import sbt.IO._
    println("r.js -o build.js".!!)
    val optimizedWebapp = new File("target/webapp-rjs")
    assertDirectory(optimizedWebapp)
    delete(origWebapp)
    delete(optimizedWebapp / "build.txt")
    copyDirectory(source = optimizedWebapp, target = origWebapp)
  }
}

// xsbt-web-plugin config
enablePlugins(TomcatPlugin)

containerLibs in Tomcat := Seq(("com.github.jsimone" % "webapp-runner" % "8.0.24.0").intransitive())

containerArgs in Tomcat := Seq("--enable-ssl", "--port", "8443")

javaOptions in Tomcat ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8788",
  "-Djavax.net.ssl.keyStore=misc/ssl/localKeystore.jks",
  "-Djavax.net.ssl.keyStorePassword=49grklgioy9048udfgge034"
)


// sbt-jasmine-plugin config
seq(jasmineSettings : _*)

appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" }

appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" }

jasmineTestDir <+= sourceDirectory { src => src / "test" / "webapp" / "js" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "test.dependencies.js" }

jasmineRequireJsFile <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" / "require.js" }

jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "require.conf.js" }

jasmineEdition := 2

parallelExecution in Test := false

test in Test <<= (test in Test) dependsOn jasmine
