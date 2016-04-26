# LC-2200-Simulator
Project for Junior Design at Georgia Tech


# How to Compile / Develop
Install Scala, SBT, and Java/JavaFX.

You can then run "sbt run" to have SBT take care of building and running the application.
Note that this project uses ScalaFX, which will be automatically downloaded by SBT, as it is a dependency in the build.sbt file, but ScalaFX depends on JavaFX, but does nothing to install JavaFX.

(Add the UserManual.pdf to this top level folder before running this command, so that it can be packaged into the resulting zip)

run ./packge_into_zip.sh

to actually package the application into a zip file. This will call "sbt assembly" (a plugin that sbt should automatically install) to create a fat jar file and then copy it, all of the .json files, and all of the .md files into a LC2200DatapathSimulator folder and then zip this folder into a zip file of the same name.

In order to not include some of the json files, you should edit jsonloader.scala to remove the load calls that load them and then not include the json files in your distribution.



