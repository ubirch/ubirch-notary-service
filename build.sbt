packagedArtifacts in file(".") := Map.empty // disable publishing of root project

lazy val testConfiguration = "-Dconfig.resource=" + Option(System.getProperty("test.config")).getOrElse("application.dev.conf")

lazy val commonSettings = Seq(

  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature"),

  version := "0.3.4",

  organization := "com.ubirch.notary",
  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/ubirch/ubirch-notary-service"),
    "https://github.com/ubirch/ubirch-notary-service"
  )),

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),

  javaOptions in Test += testConfiguration

)

/*
 * MODULES
 ********************************************************/

lazy val notaryService = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(server, model, core, client)

lazy val server = project
  .settings(commonSettings: _*)
  .settings(mergeStrategy: _*)
  .dependsOn(model, core)
  .settings(
    mainClass in assembly := Some("com.ubirch.notary.Boot"),
    libraryDependencies ++= depServer,
    resourceGenerators in Compile += Def.task {
      generateDockerFile(baseDirectory.value / ".." / "Dockerfile.input", (assemblyOutputPath in assembly).value)
    }.taskValue
  )

lazy val model = project
  .settings(commonSettings: _*)

lazy val core = project
  .settings(commonSettings: _*)
  .dependsOn(model)
  .settings(
    libraryDependencies ++= depCore,
    resolvers ++= Seq(
      resolverHasher
    )
  )

lazy val client = project
  .settings(commonSettings: _*)
  .dependsOn(model)
  .settings(
    libraryDependencies ++= depClientRest,
    resolvers ++= Seq(
      resolverBeeClient,
      resolverSeebergerJson,
      resolverHasher
    )
  )

/*
 * MODULE DEPENDENCIES
 ********************************************************/

val akkaV = "2.4.17"
val sprayV = "1.3.3"
val json4sV = "3.4.2"
val scalaTestV = "3.0.1"

lazy val depServer = Seq(

  bitcoinj,

  // Spray
  "io.spray" %% "spray-can" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "io.spray" %% "spray-client" % sprayV,
  "org.json4s" %% "json4s-jackson" % "3.2.10",

  "com.typesafe.akka" %% "akka-actor" % akkaV,

  // misc
  "joda-time" % "joda-time" % "2.9.3"

) ++ scalaLogging

lazy val depCore = Seq(
  bitcoinj,
  ubirchUtilCrypto,
  ubirchUtilConfig
) ++ scalaLogging

lazy val depClientRest = {
  Seq(
    beeClient,
    ubirchUtilConfig,
    ubirchUtilCrypto % "test",
    scalaTest % "test",
    json4sNative,
    ubirchUtilJsonAutoConvert
  ) ++ scalaLogging
}

/*
 * DEPENDENCIES
 ********************************************************/

lazy val scalaLogging = Seq(
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2" exclude("org.slf4j", "slf4j-api"),
  "ch.qos.logback" % "logback-core" % "1.1.7",
  "ch.qos.logback" % "logback-classic" % "1.1.7"
)

lazy val bitcoinj = "org.bitcoinj" % "bitcoinj-core" % "0.14.7" % "compile"

lazy val beeClient = "uk.co.bigbeeconsultants" %% "bee-client" % "0.29.1"

lazy val json4sNative = "org.json4s" %% "json4s-native" % json4sV
lazy val json4sExt = "org.json4s" %% "json4s-ext" % json4sV
lazy val json4sJackson = "org.json4s" %% "json4s-jackson" % json4sV

lazy val ubirchUtilConfig = "com.ubirch.util" %% "config" % "0.2.0"
lazy val ubirchUtilCrypto = "com.ubirch.util" %% "crypto" % "0.4.7"
lazy val ubirchUtilJsonAutoConvert = "com.ubirch.util" %% "json-auto-convert" % "0.3.2"

lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestV

/*
 * RESOLVER
 ********************************************************/

lazy val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")
lazy val resolverHasher = "RoundEights" at "http://maven.spikemark.net/roundeights"
lazy val resolverBeeClient = Resolver.bintrayRepo("rick-beton", "maven")

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("application.conf") => MergeStrategy.concat
    case m if m.toLowerCase.endsWith("application.dev.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("application.base.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("logback.xml") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("logback-test.xml") => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)

/*
 * MISC
 ********************************************************/

def generateDockerFile(file: File, jarFile: sbt.File): Seq[File] = {
  val contents =
    s"""SOURCE=server/target/scala-2.11/${jarFile.getName}
       |TARGET=${jarFile.getName}
       |""".stripMargin
  IO.write(file, contents)
  Seq(file)
}
