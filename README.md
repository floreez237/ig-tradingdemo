# Running the project

This project uses IG Rest API client to for one feature: To synchronously place a market order and
get the account summary and order details.

##Installing dependencies
Two depedencies are not available at the level of Maven Central but as jars in the 
[lib folder](lib). To install them first run the `mvnw clean` command.

##Important Properties
The IG client needs 4 important properties for it to work properly:
- The destination url (`ig.api.domain.URL`)
- The Username (`ig.username`)
- The password (`ig.password`)
- The Api Key (`ig.api-key`)

All these properties should be set in the [properties file](src/main/resources/application.properties)

##Starting the application
To start the application, run the command: `mvnw spring-boot:run`.

This automatically starts running the process that randomly places order.