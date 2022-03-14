# Running the project
(Add command to install rest api jar as light bender is iinstalled in demo project)


This project depends on the IG REST Client jar. So to be able to run the project, client jar must be locally installed 
into your maven repository. To do that, run the following command:

<b>Linux</b>
```
mvn install:install-file \
   -Dfile=<path-to-client-jar> \
   -DgroupId=com.iggroup.webapi \
   -DartifactId=ig-webapi-java-client \
   -Dversion=2.2.0-SNAPSHOT \
   -Dpackaging=jar \
   -DgeneratePom=true
```

<b>Windows</b>
```
mvn install:install-file ^
   -Dfile=<path-to-client-jar> ^
   -DgroupId=com.iggroup.webapi ^
   -DartifactId=ig-webapi-java-client ^
   -Dversion=2.2.0-SNAPSHOT ^
   -Dpackaging=jar ^
   -DgeneratePom=true
```

Then run `mvn clean` to import all dependencies into your project.

##Important Properties
The IG client needs 4 important properties for it to work properly:
- The destination url (`ig.api.domain.URL`)
- The Username (`ig.username`)
- The password (`ig.password`)
- The Api Key (`ig.api-key`)

All these properties should be set in the [properties file](src/main/resources/application.properties)

##Creating the JAR
Finally, you can now run `mvn install` to get the project's jar.