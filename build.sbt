

val scalafx = "org.scalafx" %% "scalafx" % "8.0.60-R9"
val jsonlib = "org.json" % "json" % "20160212"

lazy val commonSettings = Seq(
  organization := "xyz.room409.lc2200",
  version := "1.33.7"
)

lazy val root = (project in file(".")).
settings(commonSettings: _*).
settings(
  name := "lc2200-simulator",
  libraryDependencies += scalafx,
  libraryDependencies += jsonlib
)

fork in run := true
