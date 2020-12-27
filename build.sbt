import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "DataEngineerAssignment"

version := "1.0"

scalaVersion := "2.12.8"

libraryDependencies ++= loggingLibraries
libraryDependencies ++= configLibraries
libraryDependencies ++= zioLibraries
libraryDependencies ++= catsLibraries

outputStrategy := Some(StdoutOutput)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
