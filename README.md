# Product and Order Management
This is a prototype Product and Order mangement Application using mostly out-of-the-box Spring Boot dependencies, for example:
- REST Controller
- JPA
- JUnit 4, Mockito and Spring's  MockMvc*

Gradle has been used to configure the build. A Dockerfile has also been provided and to build an image, run:
`./gradlew build docker` at the project root. This will add the image `jasonrharris/product_and_order_service` into a local Docker instance. To run, execute `docker run -p 8080:8080 -t jasonrharris/product_and_order_service`

An H2 instance has been specified to provide Persistence, as per `resources/application.properties`. Note, once the Application is closed, the data is not currently persisted to disk.

`resources/data.sql` contains two products and prices `inserts` (along with sequence declarations) which are used to give the Application something to work with at start up.

## Assumptions and Decisions
- Only one current price is required per Product
- A Price History is required
- Although a Price contains a Currency, mostly for completeness, supporting multiple currencies on the server is not required
- When a Price change is made to a Product, it is added to the top of the Price History by Date and this becomes the current Price
- Orders consist of a number of Order Items, each of which is for one Product but can contain any number of them.
- When Orders are placed, a reference to the current price is added to the Order. This doesn't change, so I've assumed that the historical price will not be modified and so the API will not provide a way to edit historical prices. The alternative is to hard write the price into the Order Item table.

### Usage and Documentation

Once the container is running, go to http://localhost:8080/productAndOrderManager/swagger-ui.html to see how the REST API is structured and to try out examples. There are no orders set up in the initial data set, so `orders (POST)` will need to be called a few times to then test the 'retrieve all within date range' call. To find product and price ids, execute the `products` GET call.

To view the generated schema, go to http://localhost:8080/productAndOrderManager/h2-console. The credentials are set in the `resources/application.properties` file.

## Authentication Suggestions
Assuming this REST API is to be used by mobile App or web-based clients, then Token Based Authentication, over SSL, should be used. Spring provides support for JWT, though it requires some Dev effort to set up:
- the main advantage over Basic Authentication is that the user's credentials are sent only once, after which the client only needs to include the token in each request, as a Header (Plus the server does not need to re-check the credential on each request). 
- the main advantage over session based authentication is that JWT allows for stateless handling of requests as the server does not need to maintain a session per user.

## Service Redundancy
To make the overall service resilient, redundancy needs to be provided at both the service and persitence layer. Obviously we'd need to migrate away from H2 to a stand alone Database. This should be set up with at least 2 nodes, one a master DB and the other a slave (more than one slave node is a possibility). In normal usage, the master DB node is used for all writes, which are then replicated to the slave. Reading can happen from either DB node. If the master were to go down, the slave DB can take over write operations until the master is up and running again. Note that the ratio of read to write operations ought to be taken into account. It might be that one master DB is not able to handle all the expected traffic, in which case sharding of the database needs to be considered. In this case, there are multiple masters (each potentially with their on slaves), each responsible for writing data for one logical 'horizontal' split of the data (for example, perhaps orders can be split by particular product types?). Depending on the frequency of needing to combine order data (in my sharding example), the there may be a downside to consider. This could be handled by some kind of reporting data extraction process that periodically generates a report from across the cluster, as long as these cross master reads don't impact performance.

The Application itself can also be run on multiple nodes, with a load balancer in front of them to direct traffic to one or other node, probably on a round-robin basis. Each node would then write to the master and read from the slave DBs nodes as required.
