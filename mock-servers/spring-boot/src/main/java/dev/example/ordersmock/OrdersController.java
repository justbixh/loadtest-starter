package dev.example.ordersmock;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);
    private final long responseDelayMs;

    public OrdersController(@Value("${mock.response-delay-ms:0}") long responseDelayMs) {
        this.responseDelayMs = responseDelayMs;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/api/orders")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> order) {
        delayResponse();
        log.info("POST /api/orders -> 201");
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "accepted",
                "message", "Order accepted",
                "orderId", "mock-order-001"));
    }

    private void delayResponse() {
        if (responseDelayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(responseDelayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Mock response delay interrupted", exception);
        }
    }
}
