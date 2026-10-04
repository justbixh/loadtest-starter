# Local API load-test starter

A reusable load-test demo with the same order API implemented three ways: a dependency-free Python stub, Mockoon, or Spring Boot. Load it with k6 or Gatling; both scenarios read varied payloads from the same CSV file.

## Set the project root once

In each terminal, start from the project root and source `.env`:

```sh
set -a
. ./.env
set +a
```

The root [`.env`](.env) sets `LOAD_BASE` to the current project root and `MOCK_API_URL` to the local API address. Startup and load-test commands use these shared values; update `LOAD_BASE` only if you source `.env` from somewhere other than the project root.

## Pick a mock server

All three servers implement `GET /health` and `POST /api/orders` on port 3000. Start exactly one server at a time.

Each server logs a short request/status line so you can see requests arrive. Mockoon logs full request/response transactions; disable `--log-transaction` in its start script for quieter, more representative high-load runs.

### Python (no dependencies)

Requires Python 3:

```sh
./mock-server-python/start.sh
```

### Mockoon CLI

Requires Node.js 18+ (npm is included). Install Node.js, for example on macOS with Homebrew:

```sh
brew install node
npm install --global @mockoon/cli
./mock-server-mockoon/start.sh
```

### Spring Boot

Requires Java 17+. The Spring project includes its own Maven wrapper, so Maven does not need to be installed separately:

```sh
cd "$LOAD_BASE/mock-server-spring-boot"
./mvnw spring-boot:run
```

Alternatively, from the project root use the helper:

```sh
cd "$LOAD_BASE"
./mock-server-spring-boot/start.sh
```

The first run downloads Maven and the project dependencies. Press `Ctrl+C` in the server terminal to stop it.

## Tune Spring Boot and Tomcat

Settings are in [`application.yml`](mock-server-spring-boot/src/main/resources/application.yml). It exposes Tomcat worker threads, spare threads, queued connections, max connections, connection/keep-alive timeouts, and JMX metrics. `MOCK_RESPONSE_DELAY_MS` adds a delay to order creation to simulate downstream work.

For example, with the server stopped, source `.env` in the project root, then run Spring with a 20-thread pool, a queue of 10, and a 100 ms response delay:

```sh
cd "$LOAD_BASE/mock-server-spring-boot"
TOMCAT_MAX_THREADS=20 TOMCAT_ACCEPT_COUNT=10 MOCK_RESPONSE_DELAY_MS=100 ./mvnw spring-boot:run
```

If you change the server port with `SERVER_PORT`, also update `MOCK_API_URL` in `.env` to match.

## Run a load test

Install k6: on macOS with Homebrew use `brew install k6`; on Ubuntu/Debian use `sudo snap install k6`. See the [k6 installation guide](https://grafana.com/docs/k6/latest/set-up/install-k6/) for other platforms.

With one mock server running in terminal 1, source `.env` from the project root in another terminal, then run k6:

```sh
cd "$LOAD_BASE/k6"
k6 run order-create.js
```

Or run Gatling (requires Java 17+; the included Maven wrapper downloads Maven on first use):

```sh
cd "$LOAD_BASE/gatling"
./mvnw gatling:test -Dgatling.simulationClass=simulations.OrderCreateSimulation
```

The package name is `simulations` (plural), matching the declaration in `OrderCreateSimulation.java`.

Both tests default to 10 requests per second for 30 seconds. Override the rate and duration for k6 with:

```sh
cd "$LOAD_BASE/k6"
k6 run -e RATE=25 -e DURATION=1m order-create.js
```

For Gatling:

```sh
cd "$LOAD_BASE/gatling"
./mvnw gatling:test \
  -Dgatling.simulationClass=simulations.OrderCreateSimulation \
  -Drate=25 \
  -DdurationSeconds=60
```

Gatling writes an HTML report under `gatling/target/gatling/`. Run only one load generator at a time when comparing results.

## API

| Method | Path | Response |
|---|---|---|
| `GET` | `/health` | `{"status":"ok"}` |
| `POST` | `/api/orders` | `{"status":"accepted","message":"Order accepted","orderId":"mock-order-001"}` |

The response is intentionally static; this project demonstrates load-test setup and server behavior, not business logic.

## Change payloads

Edit [`gatling/src/test/resources/orders.csv`](gatling/src/test/resources/orders.csv). Both load generators read this file. Gatling cycles its CSV feeder; k6 cycles through its rows. Keep values simple (no commas or quoted fields).

## Project layout

```text
.
├── .env
├── gatling/
├── k6/
├── mock-server-mockoon/
├── mock-server-python/
└── mock-server-spring-boot/
    └── src/main/resources/application.yml
```
