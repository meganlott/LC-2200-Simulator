#/bin/sh

sbt assembly
mkdir LC2200DatapathSimulator
cp target/scala-2.10/lc2200-simulator-assembly-1.33.7.jar ./LC2200DatapathSimulator
cp -r instructions ./LC2200DatapathSimulator
cp *.md ./LC2200DatapathSimulator
cp CPU.png ./LC2200DatapathSimulator
zip LC2200DatapathSimulator.zip ./LC2200DatapathSimulator/* ./LC2200DatapathSimulator/instructions/*

