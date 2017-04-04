import com.github.play2war.plugin._

enablePlugins(PlayScala)

enablePlugins(TomcatPlugin)

organization := "com.avyeyes"

name := "AvyEyes"

version := "2.0.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation"
)

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= {
  Seq(
    jdbc,
    "org.json4s" %% "json4s-jackson" % "3.5.1",
    "com.typesafe.akka" %% "akka-actor" % "2.3.13",
    "org.postgresql" % "postgresql" % "9.4-1202-jdbc41",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.10.15",
    "org.apache.commons" % "commons-lang3" % "3.4",
    "com.google.guava" % "guava" % "18.0",

    "org.specs2" %% "specs2" % "2.4.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
    "com.h2database" % "h2" % "1.4.188" % "test",
    "javax.servlet" % "servlet-api" % "2.5" % "test"
  )
}

Play2WarKeys.servletVersion := "3.0"

Play2WarKeys.explodedJar := true

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
containerLibs in Tomcat := Seq(("com.github.jsimone" % "webapp-runner" % "8.0.24.0").intransitive())

containerArgs in Tomcat := Seq("--enable-ssl", "--port", "8443")

javaOptions in Tomcat ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8788",
  "-Djavax.net.ssl.keyStore=misc/ssl/localKeystore.jks",
  "-Djavax.net.ssl.keyStorePassword=49grklgioy9048udfgge034"
)


// sbt-jasmine-plugin config
Seq(jasmineSettings : _*)

appJsDir <+= { sourceDirectory { src => src / "public" / "javascripts" } }

appJsLibDir <+= sourceDirectory { src => src / "public" / "javascripts" / "lib" }

jasmineTestDir <+= sourceDirectory { src => src / "test" / "public" / "javascripts" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "public" / "javascripts" / "test.dependencies.js" }

jasmineRequireJsFile <+= sourceDirectory { src => src / "public" / "javascripts" / "lib" / "require.js" }

jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "public" / "javascripts" / "require.conf.js" }

jasmineEdition := 2

parallelExecution in Test := false

test in Test <<= (test in Test) dependsOn jasmine
