package simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class OrderCreateSimulation extends Simulation {

    private static final String MOCK_API_URL = System.getenv().getOrDefault("MOCK_API_URL", "http://127.0.0.1:3000");
    private static final double RATE = Double.parseDouble(System.getProperty("rate", "10"));
    private static final int DURATION_SECONDS = Integer.getInteger("durationSeconds", 30);

    private final FeederBuilder<String> orders = csv("orders.csv").circular();

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(MOCK_API_URL)
            .contentTypeHeader("application/json");

    private final String requestBody = """
            {
              "sku": "#{sku}",
              "quantity": #{quantity},
              "unitPrice": #{unitPrice},
              "customerType": "#{customerType}"
            }
            """;

    private final ScenarioBuilder createOrder = scenario("Create order")
            .feed(orders)
            .exec(
                    http("POST /api/orders")
                            .post("/api/orders")
                            .body(StringBody(requestBody)).asJson()
                            .check(
                                    status().is(201),
                                    jsonPath("$.status").is("accepted"),
                                    jsonPath("$.orderId").exists()
                            )
            );

    {
        setUp(createOrder.injectOpen(constantUsersPerSec(RATE).during(DURATION_SECONDS)))
                .protocols(httpProtocol);
    }
}
