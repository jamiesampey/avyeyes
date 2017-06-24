import com.joescii.SbtJasminePlugin._
import play.sbt.routes.RoutesKeys
import sbt.Keys.baseDirectory

organization := "com.avyeyes"

name := "AvyEyes"

version := "1.3.0"

scalaVersion := "2.11.11"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation"
)

RoutesKeys.routesImport ++= Seq(
  "com.avyeyes.controllers.TableQueryBinder._",
  "com.avyeyes.controllers.SpatialQueryBinder._"
)

libraryDependencies ++= {
  val playVersion = _root_.play.core.PlayVersion.current

  Seq(
    filters,
    jdbc,
    "com.typesafe.play" %% "play-slick" % "2.1.0",
    "com.typesafe.play" %% "play-mailer" % "5.0.0",
    "ws.securesocial" %% "securesocial" % "3.0-M8",   // TODO upgrade to 3.0 when released
    "org.postgresql" % "postgresql" % "9.4-1202-jdbc41",
    "org.json4s" %% "json4s-jackson" % "3.5.1",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.10.15",
    "joda-time" % "joda-time" % "2.9.9",
    "org.apache.commons" % "commons-lang3" % "3.4",
    "com.google.guava" % "guava" % "18.0",
    "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8",

    "com.typesafe.play" %% "play-specs2" % playVersion % Test,
    "org.scalacheck" %% "scalacheck" % "1.12.4" % Test,
    "com.h2database" % "h2" % "1.4.188" % Test,
    "javax.servlet" % "servlet-api" % "2.5" % Test
  )
}

pipelineStages := Seq(rjs)

test in Test <<= (test in Test) dependsOn jasmine

lazy val avyeyes = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtWeb)
  .settings(jasmineSettings: _*)
  .settings(
    jasmineEdition := 2,
    appJsDir += baseDirectory.value / "public" / "javascripts",
    appJsLibDir += baseDirectory.value / "public" / "javascripts" / "lib",
    jasmineTestDir += baseDirectory.value / "test" / "javascripts",
    jasmineConfFile += baseDirectory.value / "test" / "javascripts" / "test.dependencies.js",
    jasmineRequireJsFile += baseDirectory.value / "public" / "javascripts" / "lib" / "require.js",
    jasmineRequireConfFile += baseDirectory.value / "test" / "javascripts" / "require.conf.js"
  )

//lazy val mode = taskKey[String]("Build mode (dev or prod)")
//
//mode := sys.props.getOrElse("mode", default = "dev")
//
//webappPostProcess := { origWebapp =>
//  if (mode.value == "prod") {
//    import sbt.IO._
//    println("r.js -o build.js".!!)
//    val optimizedWebapp = new File("target/webapp-rjs")
//    assertDirectory(optimizedWebapp)
//    delete(origWebapp)
//    delete(optimizedWebapp / "build.txt")
//    copyDirectory(source = optimizedWebapp, target = origWebapp)
//  }
//}
