name := "tfbot"

lazy val commonSettings = Seq(
  version := "1.0.0",
  organization := "com.mairo",
  scalaVersion := "2.12.7",
  test in assembly := {}
)

libraryDependencies ++= Seq(

)

lazy val app = (project in file(".")).
  settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("com.mairo.Launcher"),
    assemblyJarName in assembly := "tfbot.jar",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-language:higherKinds",
      "-language:postfixOps",
//      "-feature",
      "-Xfatal-warnings"
    ),
    libraryDependencies ++= Seq(
      "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.typelevel" %% "cats-effect" % "2.1.3",
      "com.softwaremill.sttp" %% "async-http-client-backend-cats" % "1.7.2",
      "com.softwaremill.sttp" %% "spray-json" % "1.7.2",
      "com.typesafe" % "config" % "1.4.0"
    )
  )