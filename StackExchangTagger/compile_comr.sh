#!/bin/bash
#Coocurrence Map-reduce version compilation script.

if [ ! -d "classes" ]; then
    echo "Create classes folder..."
    mkdir classes
fi

echo "Compiling..."
javac -classpath ./lib/hadoop-client-1.2.1.jar:./lib/hadoop-core-1.2.1.jar:./lib/hadoop-tools-1.2.1.jar:./lib/commons-lang3-3.1.jar:./lib/opencsv-2.3.jar:./lib/lingpipe-4.1.0.jar -sourcepath src/org/h2t2/setagger/core/Cooccurrence.java src/org/h2t2/setagger/core/*.java src/org/h2t2/setagger/util/*.java -d classes

echo "Creating JAR file..."
jar -cvf CoocurrenceMR.jar -C classes/ .

echo "Done."
