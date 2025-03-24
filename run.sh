#!/bin/bash

# Compile the project
mvn clean package -DskipTests

# Get all dependency classpaths
CP=$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)

# Run the application, add all modules
java --module-path $CP:./target/classes \
     --add-modules ALL-MODULE-PATH \
     -m com.example.touchtyped/com.example.touchtyped.app.Application 