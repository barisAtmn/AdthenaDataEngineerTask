import sbt._
object Dependencies {

  val zioVersion   = "1.0.3"
  val pureConfigVersion = "0.13.0"
  val zioLoggingVersion = "0.5.2"

  lazy val loggingLibraries = Seq(
    "dev.zio" %% "zio-logging" % zioLoggingVersion,
    "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
    "org.slf4j" % "slf4j-simple" % "1.7.30" % Runtime
  )

  lazy val zioLibraries  = Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-test" % zioVersion % Test
  )

  lazy val configLibraries = Seq(
     "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
  )

}
