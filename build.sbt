import Dependencies._

name := "DataEngineerAssignment"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= loggingLibraries
libraryDependencies ++= configLibraries
libraryDependencies ++= zioLibraries
libraryDependencies ++= catsLibraries

fork in run := true
outputStrategy := Some(StdoutOutput)
