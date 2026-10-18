# Gym CRM

Spring (no Spring Boot) module for a gym CRM: trainees, trainers and trainings, persisted in a relational database with JPA and Hibernate.

This is the second version of the project. The first version kept everything in in-memory maps; this one stores it in a database.

## Overview

**Data model**

- `User` holds first name, last name, username, password and the active flag. `Trainee` and `Trainer` each have a one-to-one relation to their `User`.
- `Trainee` and `Trainer` have a many-to-many relation (join table `trainee_trainer`, owned by `Trainee`).
- `Training` belongs to one trainee and one trainer (foreign keys) and has a `TrainingType`.
- `TrainingType` is a constant list (`import.sql`). The entity is `@Immutable` and has no write DAO, so the application cannot change it. It is a separate table because many trainings and trainers share the same few values.
- Deleting a trainee is a hard delete. It cascades to the trainee's user and trainings.

**Layers**

| Layer | Package | Notes |
| --- | --- | --- |
| Facade | `facade` | `GymFacade`, the single entry point |
| Services | `service` | transactions (`@Transactional`), validation, authentication, username/password generation |
| DAOs | `dao`, `dao.jpa` | `EntityManager`, JPQL and the Criteria API |
| Entities | `model` | JPA entities |
| Config | `config` | Java-based Spring configuration, Hibernate, Hikari pool, transaction manager |

**Rules**

- Every operation except creating a profile needs a valid username and password, and works on the caller's own profile. Wrong credentials throw `AuthenticationFailedException`.
- Username is `First.Last`, with a serial number added when it is taken (`John.Smith1`). Password is a random 10-character string.
- Activating/deactivating a profile is not idempotent: each call flips the current state.
- Required fields are validated before create/update actions.
- A training can be added only by its trainee or its trainer.
- Passwords never appear in logs or in `toString()` output.

**Stack:** Java 21, Maven, Spring 6, Hibernate 6, H2, HikariCP, SLF4J + Logback, JUnit 5, Mockito, JaCoCo (80% line coverage enforced).

## Quick start

Requires JDK 21 and Maven 3.9+. No database needs to be installed: the default configuration uses in-memory H2.

```bash
mvn exec:java      # run the demo scenario (GymCrmApplication)
mvn verify         # run all tests and the coverage check
```

The coverage report is written to `target/site/jacoco/index.html`.

## Configuration

Database and Hibernate settings are in `src/main/resources/application.properties`. To use another DBMS (for example PostgreSQL), change `db.url`, `db.username` and `db.password` and add its JDBC driver to `pom.xml`. The schema is generated from the entities on startup (`hibernate.hbm2ddl.auto=create-drop`) and training types are loaded from `src/main/resources/import.sql`.
