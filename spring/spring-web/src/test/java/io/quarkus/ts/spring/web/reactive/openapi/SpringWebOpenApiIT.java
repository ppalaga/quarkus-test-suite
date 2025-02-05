package io.quarkus.ts.spring.web.reactive.openapi;

import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.IMAGE_10;
import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.PORT;
import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.START_LOG_10;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringWebOpenApiIT extends AbstractSpringWebOpenApiIT {
    private static Response response;

    @Container(image = IMAGE_10, port = PORT, expectedLog = START_LOG_10)
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    private static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Override
    public RestService getApp() {
        return app;
    }
}
