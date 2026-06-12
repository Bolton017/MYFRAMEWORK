#!/bin/bash

TOMCAT_LIB="/home/tommy/tomcat/lib"

JAR_NAME="framework.jar"
BIN_DIR="bin"
SRC_DIR="src/main/java"

rm -rf $BIN_DIR
rm -f $JAR_NAME
mkdir $BIN_DIR

find $SRC_DIR -name "*.java" > sources.txt
javac -cp "$TOMCAT_LIB/*" -d $BIN_DIR @sources.txt

if [ $? -ne 0 ]; then 
    echo "Erreur"
    rm sources.txt
    exit 1
fi
rm sources.txt

echo "[2/3] Creation du fichier $JAR_NAME...."
jar cf $JAR_NAME -C $BIN_DIR .

if [ $? -ne 0 ]; then
    echo "Erreur de creation de la creation du JAR"
    exit 1
fi

echo "OK"