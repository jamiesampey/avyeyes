organization := "com.avyeyes"

name := "AvyEyes"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

seq(webSettings :_*)

seq(jasmineSettings : _*)

appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" }

appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" }

jasmineTestDir <+= sourceDirectory { src => src / "test" / "webapp" / "js" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "test.dependencies.js" }

jasmineRequireJsFile <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" / "require" / "require.min.js" }

jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "require.conf.js" }

(Keys.test in Test) <<= (Keys.test in Test) dependsOn (jasmine)

libraryDependencies ++= {
  Seq(
    "net.liftweb" %% "lift-webkit" % "2.6-RC1" % "compile",
    "org.squeryl" %% "squeryl" % "0.9.5-7", 
    "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
    "org.apache.commons" % "commons-lang3" % "3.3.2",
    "com.google.guava" % "guava" % "17.0",
  	"ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")
  )
}
