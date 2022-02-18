name := "graphql-demo"

version := "0.1"

scalaVersion := "2.12.4"


val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.7"
val sangriaVersion = "2.0.0"
val sangriaCirceVersion = "1.2.1"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % sangriaVersion,
  "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8",


  "org.sangria-graphql" %% "sangria-circe" % sangriaCirceVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
  "io.circe" %%	"circe-core" % "0.9.3",
  "io.circe" %% "circe-parser" % "0.9.3",
  "io.circe" %% "circe-generic" % "0.9.3",
  "io.circe" %% "circe-optics" % "0.9.3",
  
  "com.typesafe.akka" %% "akka-http" % "10.0.4",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.4",

  "org.sangria-graphql" %% "sangria-spray-json" % "1.0.2",

  "com.typesafe.slick" %% "slick" % "3.2.3",
  "com.h2database" % "h2" % "1.4.197",
  "org.slf4j" % "slf4j-nop" % "1.7.21",
"com.softwaremill.sttp.client3" %% "core" % "3.3.17"




)
