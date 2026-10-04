# Load Test — Gatling

A Gatling-based load testing suite for simulating concurrent traffic against an HTTP API, with configurable request data via CSV feeders.

## Overview

This project uses [Gatling](https://gatling.io) to run load, soak, and stress tests against a target HTTP endpoint. Request payloads are parameterized per virtual user via a CSV feeder, so test data can be scaled or changed without touching the simulation code.

## Prerequisites

- Java 17+
- Maven (or use the bundled `./mvnw` wrapper — no local Maven install required)
- Network access to the target host

## Project Structure

```
.
├── src/test/java/simulations/
│   └── *.java                        # Simulation classes
├── src/test/resources/
│   ├── gatling.conf                  # Gatling runtime configuration
│   ├── logback.xml                   # Logging config (failed-request body dumps)
│   └── *.csv                         # Feeder data used to parameterize requests
├── results/                          # Generated HTML reports per run (survives mvn clean)
├── target/gatling/                   # Raw simulation.log files per run (wiped on mvn clean)
├── mvnw / mvnw.cmd                   # Maven wrapper
└── pom.xml
```

## Simulation Pattern

Each simulation typically:

1. Feeds the next row of test data from a CSV file via a feeder
2. Builds a request body using Gatling EL placeholders (`#{fieldName}`) resolved from the fed session data
3. Sends the HTTP request and asserts on the expected response status
4. Runs under a configured injection profile (constant rate, ramp-up, etc.)

**Feeder strategy:** `.circular()` is commonly used so a small dataset (e.g. 10 rows) can sustain a long-running test indefinitely by cycling back to the first row once exhausted. Other strategies (`.random()`, `.shuffle()`, `.queue()`) are available depending on whether repetition, exhaustion behavior, or ordering matters for the test.

### Example feeder CSV format

```csv
field1,field2,field3
value1a,value2a,value3a
value1b,value2b,value3b
```

Add or remove rows to change the pool of test data used — no code changes required.

## Configuration

### gatling.conf

Located at `src/test/resources/gatling.conf`. Key settings:

| Setting | Purpose |
|---|---|
| `core.directory.results` | Absolute path for report output — set explicitly so results survive `mvn clean` |
| `core.simulationClass` | Leave blank for an interactive simulation picker, or set to skip the prompt (see Running section) |
| `http.requestTimeout` / `readTimeout` | Per-request timeout thresholds (ms) |
| `http.ssl.useInsecureTrustManager` | Skips cert validation — fine for internal test targets only, never for production/internet-facing hosts |
| `data.writers` | `[console, file]` — live console stats plus `simulation.log` for HTML reports |

See the file itself for full inline comments on every setting.

### logback.xml

Located at `src/test/resources/logback.xml`. Can be configured to log full request/response details (including bodies) **only for failed requests**, to the console and/or a log file:

```xml
<logger name="io.gatling.http.ahc" level="DEBUG" />
<logger name="io.gatling.http.response" level="DEBUG" />
```
Bump both loggers to `TRACE` to log every request/response regardless of pass/fail (verbose — avoid for long-running tests).


## Running

> Use `.mvnw` (mvn wrapper) instead of mvn, if system doesn't have mvn installed - the flags and behavior are identical either way.

### Build

```bash
mvn -U clean verify
```
Compiles and verifies the project, including all simulation classes, without executing a load test. Useful as a pre-flight check or in CI before actually running traffic.


### Interactive (Pick a simulation)

```bash
mvn gatling:test
```
Prompts you to pick a simulation from a numbered list of all `Simulation` subclasses found under `src/test/java/simulations`.


### Run only ONE simulation

Given multiple simulations, target a specific one directly, skipping the picker:

```bash
mvn gatling:test -Dgatling.simulationClass=simulations.SalesCreateSimulation
```

Handy as a shell alias for a simulation you run often:

```bash
alias gatrun='cd ~/load-test/gatling && mvn gatling:test -Dgatling.simulationClass=simulations.SalesCreateSimulation'
```

### Non-interactive / background runs

Use `-Dgatling.simulationClass=...` for `nohup`-style background runs, CI pipelines, or scheduled jobs where there's no terminal to answer an interactive prompt:

```bash
nohup mvn gatling:test -Dgatling.simulationClass=simulations.YourSimulation &
```

### Pinning the default simulation permanently

Set directly in `gatling.conf` if you have one primary simulation:

```hocon
simulationClass = "simulations.YourSimulation"
```
Then plain `mvn gatling:test` (or `./mvnw gatling:test`) runs it directly every time, no flag needed.


## Viewing Reports

Each run produces a timestamped HTML report, e.g.:

```
target/gatling/salescreatesimulation-20251124-111200/index.html
```

or, if `core.directory.results` is set in `gatling.conf`:

```
results/yoursimulation-20260915104617145/index.html
```

If the host isn't directly browsable from your workstation, view reports via:

- **A quick local HTTP server:**
  ```bash
  cd results
  nohup python3 -m http.server 8049 &
  ```
  then browse to `http://<host>:8049` from your workstation
- **SSH tunnel:** `ssh -L 8080:localhost:8080 user@host`, then serve the report directory and open `http://localhost:8080`
- **rsync/scp:** pull the report folder to a local machine and open `index.html` directly

## Persisting Reports Outside `target/`

By default, `mvn clean` wipes `target/gatling`, taking your run history with it. A few ways to keep reports around:

### Option A: Fixed path in `pom.xml`, sibling of `target/`

```xml
<plugin>
    <groupId>io.gatling</groupId>
    <artifactId>gatling-maven-plugin</artifactId>
    <configuration>
        <resultsFolder>${project.basedir}/reports</resultsFolder>
    </configuration>
</plugin>
```

Puts reports in `<project-root>/reports/`, a sibling of `target/` that survives `mvn clean`. Add it to `.gitignore` if you don't want to commit run history.

### Option B: Absolute path outside the project entirely

Via `pom.xml`:

```xml
<resultsFolder>/home/youruser/load-test-results/gatling</resultsFolder>
```

or via `gatling.conf`:

```hocon
gatling {
  core {
    directory {
      results = "/home/youruser/load-test-results/gatling"
    }
  }
}
```

Good if you want results shared across multiple projects/branches, or backed up separately from the repo.

### Option C: Override per-run without touching config

```bash
mvn gatling:test \
  -Dgatling.simulationClass=simulations.SalesCreateSimulation \
  -Dgatling.core.directory.results=../gatling-history \
  -Dgatling.core.outputDirectoryBaseName=salescreate-smoke
```

Useful if you want some runs archived externally and others (throwaway/dev runs) still going to `target/gatling` — just omit the flags for those.

### Gotcha: relative paths

If you set `resultsFolder` (or `-Dgatling.core.directory.results`) as a **relative path** (like `../gatling-history`), it's resolved relative to the module's working directory when Maven runs — usually the project root, so `../gatling-history` lands *next to* the project, not inside it. Use `${project.basedir}/...` (in `pom.xml`) or an absolute path if you want the location anchored explicitly regardless of where `mvn` is invoked from.

## Suggested Enhancements

- Add `.assertions()` to `setUp()` for pass/fail CI-style gating (success rate, p95/p99 latency thresholds)
- Use a ramp-up injection profile instead of a flat rate to pinpoint the exact throughput failure threshold in a single run
- Export live metrics to Graphite/InfluxDB for real-time dashboard correlation with backend metrics during a run
- Add `.throttle()` as a hard RPS ceiling, separate from the injection profile
- Load-balance across multiple mock/stub servers to isolate load-test-side bottlenecks from the real target

Export live metrics to Graphite/InfluxDB instead of only the HTML report
Since you already run Grafana/Loki/Prometheus for the SND stack, Gatling has a built-in Graphite writer (gatling.conf → data.writers). That would let you watch active-user count, response time, and error rate live in a Grafana panel during the hour-long soak, correlating directly with the order-mgmt queue backlog you're tracking — rather than waiting for the run to finish and opening the static HTML report.


## Troubleshooting

- **`IllegalAccessException` on JDK 17+** — often resolved via `--add-opens` JVM flags (JPMS module access restrictions)
- **`ClassNotFoundException`** — don't include the `.java` suffix when specifying `simulationClass`
- **Reports missing after `mvn clean`** — ensure `core.directory.results` in `gatling.conf` points outside `target/`, or see [Persisting Reports Outside `target/`](#persisting-reports-outside-target) above
- **Maven can't find your simulation** — make sure the class declares `package simulations;` and lives at `src/test/java/simulations/YourSimulation.java`. The package declaration and file path must match exactly.

## References

- [Gatling Documentation](https://docs.gatling.io)
- [Gatling Injection Profiles](https://docs.gatling.io/reference/script/core/injection/)
- [Gatling Feeders](https://docs.gatling.io/reference/script/core/session/feeders/)
- [Gatling Assertions](https://docs.gatling.io/reference/script/core/assertions/)

## Useful Aliases
```
alias ga='cd /root/load-test/gatling'
alias gat='cd /root/load-test/gatling/src/test/java/simulations'
alias gatres='cd /root/load-test/gatling/results'
gatrun() {
   cd /root/load-test/gatling && mvn gatling:test -Dgatling.simulationClass=simulations.$1
}
```
