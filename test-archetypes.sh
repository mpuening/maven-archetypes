#!/bin/sh

#
# POM Project
#
mkdir -p target/archetypes
cd target/archetypes
rm -rf my-pom-project
mvn archetype:generate \
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

# Go back up
cd ../../..

#
# Empty Project
#
mkdir -p target/archetypes
cd target/archetypes
rm -rf my-empty-project
mvn archetype:generate \
    -Darchetype.interactive=false --batch-mode \
    -DarchetypeGroupId=io.github.mpuening \
    -DarchetypeArtifactId=maven-archetypes-empty \
    -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=org.example.project \
    -DartifactId=my-empty-project \
    -Dversion=0.0.1-SNAPSHOT

if [ $? -eq 0 ]; then
    echo "Empty Project created..."
else
    echo "Empty Project failed to create..."
    exit 1
fi

cd my-empty-project
mvn clean package
if [ $? -eq 0 ]; then
    echo "Empty Project built properly..."
else
    echo "Empty Project failed to build..."
    exit 1
fi

# Go back up
cd ../../..

#
# JSP Project
#
mkdir -p target/archetypes
cd target/archetypes
rm -rf my-jsp-project
mvn archetype:generate \
    -Darchetype.interactive=false --batch-mode \
    -DarchetypeGroupId=io.github.mpuening \
    -DarchetypeArtifactId=maven-archetypes-jsp-war \
    -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=org.example.project \
    -DartifactId=my-jsp-project \
    -Dversion=0.0.1-SNAPSHOT

if [ $? -eq 0 ]; then
    echo "JSP Project created..."
else
    echo "JSP Project failed to create..."
    exit 1
fi

cd my-jsp-project
mvn clean package
if [ $? -eq 0 ]; then
    echo "JSP Project built properly..."
else
    echo "JSP Project failed to build..."
    exit 1
fi

# Go back up
cd ../../..

#
# JSF Project
#
mkdir -p target/archetypes
cd target/archetypes
rm -rf my-jsf-project
mvn archetype:generate \
    -Darchetype.interactive=false --batch-mode \
    -DarchetypeGroupId=io.github.mpuening \
    -DarchetypeArtifactId=maven-archetypes-jsf-war \
    -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=org.example.project \
    -DartifactId=my-jsf-project \
    -Dversion=0.0.1-SNAPSHOT

if [ $? -eq 0 ]; then
    echo "JSF Project created..."
else
    echo "JSF Project failed to create..."
    exit 1
fi

cd my-jsf-project
mvn clean package
if [ $? -eq 0 ]; then
    echo "JSF Project built properly..."
else
    echo "JSF Project failed to build..."
    exit 1
fi

# Go back up
cd ../../..

#
# JAX-RS Project
#
mkdir -p target/archetypes
cd target/archetypes
rm -rf my-jaxrs-project
mvn archetype:generate \
    -Darchetype.interactive=false --batch-mode \
    -DarchetypeGroupId=io.github.mpuening \
    -DarchetypeArtifactId=maven-archetypes-jaxrs-war \
    -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=org.example.project \
    -DartifactId=my-jaxrs-project \
    -Dversion=0.0.1-SNAPSHOT

if [ $? -eq 0 ]; then
    echo "JAX-RS Project created..."
else
    echo "JAX-RS Project failed to create..."
    exit 1
fi

cd my-jaxrs-project
mvn clean package
if [ $? -eq 0 ]; then
    echo "JAX-RS Project built properly..."
else
    echo "JAX-RS Project failed to build..."
    exit 1
fi

# Go back up
cd ../../..

exit 0
