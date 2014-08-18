organization := "com.avyeyes"

name := "avyeyes"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

seq(webSettings :_*)

seq(jasmineSettings : _*)

appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" }

appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" }

jasmineTestDir <+= sourceDirectory { src => src / "test" / "webapp" / "js" }

jasmineConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "test.dependencies.js" }

jasmineRequireJsFile <+= sourceDirectory { src => src / "main" / "webapp" / "js" / "lib" / "require" / "require.min.js" }

jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "webapp" / "js" / "require.conf.js" }

(test in Test) <<= (test in Test) dependsOn (jasmine)

libraryDependencies ++= {
  val liftVersion = "2.5.1"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "org.squeryl" %% "squeryl" % "0.9.5-6", 
    "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
    "org.apache.commons" % "commons-lang3" % "3.3.2",
  	"ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")
  )
}
