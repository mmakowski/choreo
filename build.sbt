// Include the Android plugin
androidDefaults

// Name of your app
name := "Sparrow"

// Version of your app
version := "0.0.1"

// Version number of your app
versionCode := 0

// Version of Scala
scalaVersion := "2.10.4"

// Version of the Android platform SDK
platformName := "android-19"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies ++= Seq(
  "com.google.code.gson"  % "gson"           % "2.2.2",
  "com.google.guava"      % "guava"          % "13.0.1",
  "junit"                 % "junit"          % "4.11"    % "test",
  "org.hamcrest"          % "hamcrest-core"  % "1.3"     % "test",
  "org.specs2"           %% "specs2"         % "2.3.11"  % "test"
)
