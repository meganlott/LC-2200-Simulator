

val scalafx = "org.scalafx" %% "scalafx" % "8.0.60-R9"

lazy val commonSettings = Seq(
  organization := "xyz.room409.lc2200",
  version := "1.33.7"
)

lazy val root = (project in file(".")).
settings(commonSettings: _*).
settings(
  name := "lc2200-simulator",
  libraryDependencies += scalafx
)

fork in run := true
