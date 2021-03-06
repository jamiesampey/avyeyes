import play.sbt.routes.RoutesKeys

organization := "com.jamiesampey"

name := "avyeyes"

version := "2.0.0"

scalaVersion := "2.11.11"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation"
)

RoutesKeys.routesImport ++= Seq(
  "com.jamiesampey.avyeyes.controllers.TableQueryBinder._",
  "com.jamiesampey.avyeyes.controllers.SpatialQueryBinder._"
)

libraryDependencies ++= {
  val playVersion = _root_.play.core.PlayVersion.current

  Seq(
    filters,
    jdbc,
    "com.typesafe.play"     %% "play-slick"       % "2.1.0",
    "com.typesafe.play"     %% "play-mailer"      % "5.0.0",
    "ws.securesocial"       %% "securesocial"     % "3.0-M8",
    "org.postgresql"        % "postgresql"        % "9.4-1202-jdbc41",
    "org.json4s"            %% "json4s-jackson"   % "3.5.1",
    "com.amazonaws"         % "aws-java-sdk-s3"   % "1.10.15",
    "joda-time"             % "joda-time"         % "2.9.9",
    "org.apache.commons"    % "commons-lang3"     % "3.4",
    "com.google.guava"      % "guava"             % "19.0",
    "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8",

    "com.typesafe.play"     %% "play-specs2"      % playVersion % Test,
    "org.scalacheck"        %% "scalacheck"       % "1.12.4"    % Test,
    "com.h2database"        % "h2"                % "1.4.188"   % Test,
    "javax.servlet"         % "servlet-api"       % "2.5"       % Test
  )
}

PlayKeys.playRunHooks <+= baseDirectory.map(Webpack.apply)

routesGenerator := InjectedRoutesGenerator

excludeFilter in (Assets, JshintKeys.jshint) := "*.js"

watchSources ~= { (ws: Seq[File]) =>
  ws filterNot { path =>
    path.getName.endsWith(".js") || path.getName == "build"
  }
}

lazy val avyeyes = (project in file(".")).enablePlugins(PlayScala)

// Prod build stuff below

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

lazy val webpackProdBuild = TaskKey[Unit]("Run a webpack prod build prior to prod .zip packaging")
webpackProdBuild := { Process("npm run dist", baseDirectory.value) ! }
dist <<= dist dependsOn webpackProdBuild
