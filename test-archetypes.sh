#!/bin/sh

#
# POM Project
#
mkdir -p target/archetypes
cd target/archetypes
rm -rf my-pom-project
mvn archetype:generate -o \
    -Darchetype.interactive=false --batch-mode \
    -DarchetypeGroupId=io.github.mpuening \
    -DarchetypeArtifactId=maven-archetypes-pom \
    -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=org.example.project \
    -DartifactId=my-pom-project \
    -Dversion=0.0.1-SNAPSHOT

if [ $? -eq 0 ]; then
    echo "POM Project created..."
else
    echo "POM Project failed to create..."
    exit 1
fi

cd my-pom-project
mvn clean package
if [ $? -eq 0 ]; then
    echo "POM Project built properly..."
else
    echo "POM Project failed to build..."
    exit 1
fi

exit 0
