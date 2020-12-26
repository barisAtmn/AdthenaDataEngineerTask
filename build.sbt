import Dependencies._

name := "DataEngineerAssignment"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies ++= loggingLibraries
libraryDependencies ++= configLibraries
libraryDependencies ++= zioLibraries

fork in run := true
outputStrategy := Some(StdoutOutput)
