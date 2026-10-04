# Mock servers

If you do not already have an API to load test, start one of the included mock servers. They all implement `GET /health` and `POST /api/orders` on port 3000. Run only one at a time. Each logs a short request/status line; Mockoon logs complete transactions, so disable `--log-transaction` in `mockoon/start.sh` for quieter high-load runs.

From the project root, source `.env` in each terminal before starting a server:

```sh
set -a
. ./.env
set +a
```

## Python

Requires Python 3 and no additional packages:

```sh
./mock-servers/python/start.sh
```

## Mockoon

Requires Node.js 18+ (npm included). On macOS, install Node.js with Homebrew; on Linux follow the [Node.js installation instructions](https://nodejs.org/en/download/package-manager).

```sh
brew install node
npm install --global @mockoon/cli
./mock-servers/mockoon/start.sh
```

## Spring Boot

Requires Java 17+. The project includes a Maven wrapper, so a separate Maven installation is not required:

```sh
cd "$LOADBASE/mock-servers/spring-boot"
./mvnw spring-boot:run
```

Or use the helper from the project root:

```sh
./mock-servers/spring-boot/start.sh
```

The first run downloads Maven and the project dependencies. Press `Ctrl+C` to stop the server.

## Tune Spring Boot and Tomcat

Settings are in [`spring-boot/src/main/resources/application.yml`](spring-boot/src/main/resources/application.yml). Configure Tomcat worker/spare threads, accept queue, maximum connections, timeouts, and JMX metrics there or through the environment variables shown in the YAML. `MOCK_RESPONSE_DELAY_MS` adds an artificial delay to `POST /api/orders`.

For example, run Spring with 20 request threads, a queue of 10, and a 100 ms order response delay:

```sh
cd "$LOADBASE/mock-servers/spring-boot"
TOMCAT_MAX_THREADS=20 TOMCAT_ACCEPT_COUNT=10 MOCK_RESPONSE_DELAY_MS=100 ./mvnw spring-boot:run
```

The load-test scripts target `http://127.0.0.1:3000` by default. If you change the server port, update the hardcoded URL in both the k6 and Gatling scenarios.

## API

| Method | Path | Response |
|---|---|---|
| `GET` | `/health` | `{"status":"ok"}` |
| `POST` | `/api/orders` | `{"status":"accepted","message":"Order accepted","orderId":"mock-order-001"}` |

The order response is static by design: the mock is only a target for load-test practice.
