# Product and Order Management
This is a prototype Product and Order mangement Application using mostly out-of-the-box Spring Boot dependencies, for example:
- REST Controller
- JPA
- JUnit 4, Mockito and Spring's  MockMvc*

Gradle has been used to configure the build. A Dockerfile has also been provided and to build an image, run:
`./gradlew build docker` at the project root. This will install the image (`jasonrharris/product_and_order_service`) into a local Docker instance. To run, execute `docker run -p 8080:8080 -t jasonrharris/product_and_order_service`

An H2 instance has been specified as the Persistence layer, as in `resources/application.properties`. Note, once the Application is closed, the data is not currently persisted.

`resources/data.sql` contains two products and prices which are used to give the Application something to work with at start-up.

# Authentication Suggestions
No authentication has been provided
