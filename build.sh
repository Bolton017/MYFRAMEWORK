#!/bin/bash

# Définition des variables
APP_NAME="app"
SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp"
WEB_XML="src/main/web.xml"
BUILD_DIR="build"
LIB_DIR="/home/bolton/apache-tomcat-10.0.16/lib"
TOMCAT_WEBAPPS="/home/bolton/apache-tomcat-10.0.16/webapps"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"
FRAMEWORK_JAR="framework.jar"
JAVAC_CP="$SERVLET_API_JAR"

if [ -f "$FRAMEWORK_JAR" ]; then
    JAVAC_CP="$JAVAC_CP:$FRAMEWORK_JAR"
fi

# Nettoyage et création du répertoire temporaire
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/WEB-INF/classes
mkdir -p $BUILD_DIR/WEB-INF/lib             # dossier pour les JARs embarqués

# Compilation des fichiers Java avec le JAR des Servlets + framework
find $SRC_DIR -name "*.java" > sources.txt
javac -cp "$JAVAC_CP" -d $BUILD_DIR/WEB-INF/classes @sources.txt

# Générer le JAR du framework pour réutilisation dans un autre projet
jar -cvf "$BUILD_DIR/$FRAMEWORK_JAR" -C $BUILD_DIR/WEB-INF/classes .
cp -f "$BUILD_DIR/$FRAMEWORK_JAR" "$FRAMEWORK_JAR"

# Copier le framework JAR dans WEB-INF/lib (Tomcat le charge automatiquement)
cp -f "$BUILD_DIR/$FRAMEWORK_JAR" "$BUILD_DIR/WEB-INF/lib/"

# Copier les fichiers web (JSP, assets...) s'il y en a
if [ -d "$WEB_DIR" ] && [ "$(ls -A $WEB_DIR)" ]; then
    cp -r $WEB_DIR/* $BUILD_DIR/
fi

# Copie explicite du web.xml dans WEB-INF/ si le fichier existe
if [ -f "$WEB_XML" ]; then
    cp "$WEB_XML" "$BUILD_DIR/WEB-INF/"
fi

# Générer le fichier .war dans le dossier build
cd $BUILD_DIR || exit
jar -cvf $APP_NAME.war .   # Utiliser "." au lieu de "*" évite les bugs de chemins avec jar e
cd ..

# Déploiement dans Tomcat
cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/

# Nettoyage du fichier temporaire des sources
rm -f sources.txt

echo ""
echo "Déploiement terminé. Redémarrez Tomcat si nécessaire."
echo ""