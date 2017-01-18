name := "httplogmon"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  // cli flag parsing library
  "commons-cli" % "commons-cli" % "1.3",
  // actor library for concurrency model
  "com.typesafe.akka" %% "akka-actor" % "2.4.16",
  "com.typesafe.akka" %% "akka-stream" % "2.4.16",
  "com.typesafe.akka" %% "akka-stream-contrib" % "0.6",
  // higher level data structures utiil library
  "com.google.guava" % "guava" % "21.0",
  // testing libraries
  "junit" % "junit" % "4.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.16"
  )

assemblyJarName in assembly := "httplogmon.jar"

mainClass in assembly := Some("HttpLogMonitor")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf"            => MergeStrategy.concat
  case "reference.conf"              => MergeStrategy.concat
  case x =>
    val baseStrategy = (assemblyMergeStrategy in assembly).value
    baseStrategy(x)
}
