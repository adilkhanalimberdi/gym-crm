# Gym CRM

Spring Core (no Spring Boot) module that handles a gym CRM: trainees, trainers and trainings, stored in in-memory maps.

## Overview

- **Services:** `TraineeService` (create/update/delete/select), `TrainerService` (create/update/select), `TrainingService` (create/select), exposed through `GymFacade`.
- **Storage:** one `java.util.Map` bean per entity type, filled at startup from `src/main/resources/data/seed-data.txt` by a `BeanPostProcessor`. The file path is set in `application.properties` (`storage.init.file`).
- **Usernames and passwords:** username is `First.Last`, with a serial number appended on duplicates (`John.Smith1`). Passwords are random 10-character strings.
- **Configuration:** Java-based (`AppConfig`, `StorageConfig`) with component scanning.
- **Stack:** Java 21, Maven, Spring 6, SLF4J + Logback, JUnit 5, Mockito, JaCoCo (80% line coverage enforced).

## Quick start

Requires JDK 21 and Maven 3.9+.

```bash
mvn exec:java      # run the demo application
mvn verify         # run tests and the coverage check
```

The coverage report is written to `target/site/jacoco/index.html`.
