package io.quarkus.ts.monitoring.opentracing.reactive.grpc;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;

public abstract class AbstractPingPongResourceIT {

    private static final String PING_ENDPOINT = "/%s-ping";
    private static final String PONG_ENDPOINT = "/%s-pong";

    private static final String PING_RESOURCE = "PingResource";
    private static final String PONG_RESOURCE = "PongResource";

    @JaegerContainer
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.jaeger.endpoint", jaeger::getRestUrl);

    @Test
    public void testPingPong() {
        // When calling ping, the rest will invoke also the pong rest endpoint.
        given()
                .when().get(pingEndpoint())
                .then().statusCode(HttpStatus.SC_OK)
                .body(containsString("ping pong"));

        // Then both ping and pong rest endpoints should have the same trace Id.
        String pingTraceId = given()
                .when().get(pingEndpoint() + "/lastTraceId")
                .then().statusCode(HttpStatus.SC_OK).and().extract().asString();

        assertTraceIdWithPongService(pingTraceId);

        // Then Jaeger is invoked
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> given()
                .when().get(jaeger.getTraceUrl() + "?traceID=" + pingTraceId)
                .then().statusCode(HttpStatus.SC_OK)
                .and().body(allOf(containsString(PING_RESOURCE), containsString(PONG_RESOURCE))));
    }

    protected abstract String endpointPrefix();

    protected void assertTraceIdWithPongService(String expected) {
        String pongTraceId = given()
                .when().get(pongEndpoint() + "/lastTraceId")
                .then().statusCode(HttpStatus.SC_OK).and().extract().asString();

        assertEquals(expected, pongTraceId);
    }

    private String pingEndpoint() {
        return String.format(PING_ENDPOINT, endpointPrefix());
    }

    private String pongEndpoint() {
        return String.format(PONG_ENDPOINT, endpointPrefix());
    }
}
