# Gym CRM

REST API for a gym CRM: trainees, trainers and trainings, built with Spring Boot and persisted in a relational database with JPA and Hibernate.

This is the fourth version of the project. The first kept everything in in-memory maps, the second moved it to a database (Spring + Hibernate), the third put a REST API on top of it, this one adds monitoring and separate environments.

## Overview

**Data model**

- `User` holds first name, last name, username, password and the active flag. `Trainee` and `Trainer` each have a one-to-one relation to their `User`.
- `Trainee` and `Trainer` have a many-to-many relation (join table `trainee_trainer`, owned by `Trainee`).
- `Training` belongs to one trainee and one trainer and has a `TrainingType`, which is the specialization of the trainer.
- `TrainingType` is a constant list (`import.sql`). The entity is `@Immutable` and has no write DAO, so the application cannot change it.
- Deleting a trainee is a hard delete. It cascades to the trainee's user and trainings.

**Layers**

| Layer | Package | Notes |
| --- | --- | --- |
| Controllers | `web.controller` | `@RestController`s documented with Swagger annotations |
| DTOs and mapping | `dto.request`, `dto.response`, `mapper` | one request type per use case (validated once, at the REST boundary), response objects, one `GymCrmMapper` |
| Web support | `web.error`, `web.security`, `web.logging` | error handling (domain exceptions carry their HTTP status), Basic credentials, logging filters |
| Monitoring | `health`, `metrics` | custom health indicators and Prometheus counters |
| Services | `service` | transactions (`@Transactional`), authentication, username/password generation |
| DAOs | `dao`, `dao.jpa` | `EntityManager`, JPQL and the Criteria API |
| Entities | `model` | JPA entities |

**Rules**

- Registration (`POST /api/trainees`, `POST /api/trainers`) and login need no credentials. Every other endpoint needs the caller's username and password in an HTTP Basic `Authorization` header and works on the caller's own data (`401` for wrong credentials, `403` for someone else's data).
- Username is `First.Last`, with a serial number added when it is taken (`John.Smith1`). Password is a random 10-character string. Both are returned once, in the registration response.
- Activating/deactivating a user is not idempotent: asking for the state the user is already in answers `409 Conflict`.
- A training can be added only by its trainee or its trainer. Trainings cannot be updated or deleted.
- Passwords never appear in logs or in `toString()` output.

## Endpoints

| Method and path | Purpose | Success |
| --- | --- | --- |
| `POST /api/trainees` | Register a trainee | 201 + credentials |
| `POST /api/trainers` | Register a trainer (`specializationId` = training type id) | 201 + credentials |
| `GET /api/auth/login?username=&password=` | Check credentials | 200 |
| `PUT /api/auth/password` | Change password (`username`, `oldPassword`, `newPassword`) | 200 |
| `GET /api/trainees/{username}` | Trainee profile with trainers | 200 |
| `PUT /api/trainees/{username}` | Update trainee profile | 200 + profile |
| `DELETE /api/trainees/{username}` | Delete trainee (hard, with trainings) | 200 |
| `GET /api/trainers/{username}` | Trainer profile with trainees | 200 |
| `PUT /api/trainers/{username}` | Update trainer profile (specialization is read only) | 200 + profile |
| `GET /api/trainees/{username}/unassigned-trainers` | Active trainers not assigned to the trainee | 200 |
| `PUT /api/trainees/{username}/trainers` | Replace the trainee's trainers | 200 + trainers |
| `GET /api/trainees/{username}/trainings` | Filter: `periodFrom`, `periodTo`, `trainerName`, `trainingType` | 200 |
| `GET /api/trainers/{username}/trainings` | Filter: `periodFrom`, `periodTo`, `traineeName` | 200 |
| `POST /api/trainings` | Add a training | 200 |
| `PATCH /api/users/{username}/activation` | Activate/deactivate a trainee or trainer (`isActive`) | 200 |
| `GET /api/training-types` | List training types | 200 |

Errors share one body: `timestamp`, `status`, `error`, `message`, `path`, `transactionId`.

Swagger UI is at `http://localhost:8080/swagger-ui.html`, the OpenAPI document at `/v3/api-docs`.

## Logging

- **Transaction level:** every request gets a `transactionId` (taken from the `X-Transaction-Id` header when the caller sends a harmless one, generated otherwise). It is in every log line (`[...]` in the pattern), in the `X-Transaction-Id` response header and in error bodies. `TransactionId.current()` returns it for passing on to downstream services.
- **REST call level:** `RestCallLoggingFilter` writes one line per call with the method, endpoint, status and duration. It is `INFO` for 2xx/3xx, `WARN` for 4xx and `ERROR` for 5xx. Request and response bodies contain personal data, so they are only written at `DEBUG` (passwords masked). The `Authorization` header is never logged.
- **Log level:** `INFO` by default. Enable `DEBUG` by adding the `debug` profile to an environment (`--spring.profiles.active=local,debug`) or the environment variable `LOGGING_LEVEL_COM_EPAM_GYMCRM=DEBUG`; do not do that in production.

## Monitoring

Actuator is at `/actuator`: `health` and `prometheus` in every environment, `info` and `metrics` in `local` and `dev` only. Actuator endpoints have no authentication of their own, so `/actuator` should stay reachable only from inside the deployment network.

**Custom health indicators** (details are shown in `local` and `dev`):

| Component | Down when |
| --- | --- |
| `trainingTypes` | no training types are loaded or the database cannot be read |
| `connectionPool` | threads are waiting for a database connection and none is free |
| `heapMemory` | used heap is over `gym-crm.health.heap-max-used-ratio` (0.9 by default) |

**Custom Prometheus metrics:**

| Metric | Tags |
| --- | --- |
| `gym_crm_registrations_total` | `role` = `trainee`, `trainer` |
| `gym_crm_authentications_total` | `result` = `success`, `failure` |
| `gym_crm_trainings_created_total` | `training_type` |

## Quick start

Requires JDK 21 and Maven 3.9+. No database needs to be installed: the default `local` profile uses in-memory H2.

```bash
mvn spring-boot:run   # start the API on port 8080
mvn verify            # run all tests and the coverage check (80% line coverage enforced)
```

The coverage report is written to `target/site/jacoco/index.html`.

## Configuration

Shared settings are in `src/main/resources/application.properties`; each environment has its own `application-<profile>.properties`. Choose one with `--spring.profiles.active=<profile>` or `SPRING_PROFILES_ACTIVE`.

| Profile | Database | Connection pool | Schema |
| --- | --- | --- | --- |
| `local` (default) | in-memory H2 | 5 | generated from the entities, training types from `import.sql` |
| `dev` | PostgreSQL, `DB_URL` (default `localhost:5432/gym_crm_dev`) | 5 | Flyway, validated by Hibernate |
| `stg` | PostgreSQL, `DB_URL` | 10 | Flyway, validated by Hibernate |
| `prod` | PostgreSQL, `DB_URL` | 20 | Flyway, validated by Hibernate |

`dev`, `stg` and `prod` read `DB_USERNAME` and `DB_PASSWORD` from the environment, and `stg` and `prod` require `DB_URL` too. Migrations are in `src/main/resources/db/migration`.

**Stack:** Java 21, Maven, Spring Boot 3, Spring Boot Actuator, Micrometer + Prometheus, Hibernate 6, Flyway, PostgreSQL, H2, springdoc-openapi, Lombok, SLF4J + Logback, JUnit 5, Mockito, JaCoCo.
