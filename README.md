# Load-test starter

A reusable starter for exercising HTTP APIs with k6 or Gatling. Use your own API, or start one of the included mock servers documented in [`mock-servers/README.md`](mock-servers/README.md).

## Set the project root

From the project root, load `.env` in each terminal:

```sh
set -a; source .env; set +a
```

`LOADBASE` points to the project root and is used by the helper commands.

## Run a load test

The included examples target `http://127.0.0.1:3000`. To test another API, change the URL in both `k6/order-create.js` and `gatling/src/test/java/simulations/OrderCreateSimulation.java`.

Install k6 (macOS: `brew install k6`; Ubuntu/Debian: `sudo snap install k6`). See the [k6 installation guide](https://grafana.com/docs/k6/latest/set-up/install-k6/) for other platforms.

Run k6:

```sh
cd "$LOADBASE/k6"
k6 run order-create.js
```

Run Gatling (Java 17+ required; the included Maven wrapper downloads Maven on first use):

```sh
cd "$LOADBASE/gatling"
./mvnw gatling:test -Dgatling.simulationClass=simulations.OrderCreateSimulation
```

Both tests default to 10 requests per second for 30 seconds. For example, set k6 rate and duration with:

```sh
cd "$LOADBASE/k6"
k6 run -e RATE=25 -e DURATION=1m order-create.js
```

Gatling options can be overridden with `-Drate=25 -DdurationSeconds=60`. Reports are generated under `gatling/target/gatling/`. Run only one load generator at a time when comparing results.

## Change payloads

Edit [`gatling/src/test/resources/orders.csv`](gatling/src/test/resources/orders.csv). Both load generators read this shared CSV file; Gatling cycles its feeder and k6 cycles through the rows.

## Project layout

```text
.
├── gatling/
├── k6/
└── mock-servers/
    ├── mockoon/
    ├── python/
    ├── spring-boot/
    └── README.md
```
