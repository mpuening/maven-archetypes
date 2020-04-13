Maven Archetypes
================

This project contains a suite of Maven Archetypes.

To install these archetypes, run this command:

```
./mvnw clean install
```

JSF Application
===============

To create a simple JSF application with a login form and a Bootstrap UI that works 
with Open Liberty, run this command:

```
mvn archetype:generate -o \
    -Darchetype.interactive=false --batch-mode \
    -DarchetypeGroupId=io.github.mpuening -DarchetypeArtifactId=maven-archetypes-jsf-war -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=org.example.jsfapp -DartifactId=my-jsf-app -Dversion=0.0.1-SNAPSHOT
```

Note: replace the `groupId` and `artifactId` and `version` on the last line of the 
command as needed.


JAX-RS API
==========

To create a simple JAX-RS API that works with Open Liberty, run this command:

```
mvn archetype:generate -o \
    -Darchetype.interactive=false --batch-mode \
    -DarchetypeGroupId=io.github.mpuening -DarchetypeArtifactId=maven-archetypes-jaxrs-war -DarchetypeVersion=0.0.1-SNAPSHOT \
    -DgroupId=org.example.jaxrsapi -DartifactId=my-jaxrs-api -Dversion=0.0.1-SNAPSHOT
```

Note: replace the `groupId` and `artifactId` and `version` on the last line of the 
command as needed.
