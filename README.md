# Gym CRM

REST API for a gym CRM: trainees, trainers and trainings, built with Spring Boot and persisted in a relational database with JPA and Hibernate.

This is the third version of the project. The first kept everything in in-memory maps, the second moved it to a database (Spring + Hibernate), this one puts a REST API on top of it.

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
| Web support | `web.dto`, `web.mapper`, `web.error`, `web.security`, `web.logging` | request/response objects, error handling, Basic credentials, logging filters |
| Services | `service` | transactions (`@Transactional`), validation, authentication, username/password generation |
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
- **REST call level:** logger `rest.calls` writes one line per call with the endpoint, request body, status and response body (the error message for failed calls). It is `INFO` for 2xx/3xx, `WARN` for 4xx and `ERROR` for 5xx. Passwords are masked and the `Authorization` header is never logged.

## Quick start

Requires JDK 21 and Maven 3.9+. No database needs to be installed: the default configuration uses in-memory H2.

```bash
mvn spring-boot:run   # start the API on port 8080
mvn verify            # run all tests and the coverage check (80% line coverage enforced)
```

The coverage report is written to `target/site/jacoco/index.html`.

## Configuration

Database and logging settings are in `src/main/resources/application.properties`. To use another DBMS (for example PostgreSQL), change the `spring.datasource.*` values and add its JDBC driver to `pom.xml`. The schema is generated from the entities on startup (`spring.jpa.hibernate.ddl-auto=create-drop`) and training types are loaded from `src/main/resources/import.sql`.

**Stack:** Java 21, Maven, Spring Boot 3, Hibernate 6, H2, springdoc-openapi, SLF4J + Logback, JUnit 5, Mockito, JaCoCo.
