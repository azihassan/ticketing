## Ticketing REST API

### Installation

You can build the project from source by following these steps:

- git clone https://github.com/azihassan/ticketing
- ./mvnw package
- java -jar target/ticketing-0.0.1-SNAPSHOT.jar

The project expects an Oracle database to be running on localhost:1521. You can find the preconfigured credentials in src/main/resources/application.properties

This docker command pull and run a compatible database: `docker run -p 1521:1521 -e ORACLE_PASSWORD=oracle gvenzl/oracle-free`

You can use your own database by setting `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` environment variables to point to its credentials.

Alternatively, if you have Docker, you can run the project with `docker-compose up`. It will pull and run a preconfigured database.

### Documentation

The API is available for testing on http://localhost:8080/swagger-ui.html

There are three users: employee_demo, employee_demo_2 and it_demo. You can login by using the /login endpoint. The password is the same as the user name.

Once logged in, a remember-me cookie is stored in the browser. You can then use the remaining endpoints.
