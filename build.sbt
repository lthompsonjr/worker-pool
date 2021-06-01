scalaVersion := "2.13.5"

organization := "com.pirum"
organizationName := "Pirum Systems"
organizationHomepage := Some(url("https://www.pirum.com"))

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor" % "2.6.14",
  "com.typesafe.akka" %% "akka-stream" % "2.6.14",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.14",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % "test",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "org.scalactic" %% "scalactic" % "3.2.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.14" % Test,
)
