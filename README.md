# Shelter Testing Starter — Advanced Unit Testing

This Spring Boot project is pre-built for Advanced Unit Testing.

## Domain

A simple animal shelter with:
- Animals that can be created, listed, and adopted.
- A notification client that sends adoption alerts to an external system.

## Architecture for Testing

| Layer | Class | Test Style |
|-------|-------|------------|
| Service | `AnimalService` | Mockito unit tests |
| Controller | `AnimalController` | MockMvc with `@WebMvcTest` |
| Repository | `AnimalRepository` | `@DataJpaTest` |
| Full flow | create → adopt → notify | `@SpringBootTest` with `@MockitoBean` |

## External Dependency Seam

`NotificationClient` is the outbound interface.  
In production, `LoggingNotificationClient` logs to stdout.  
In integration tests, can be replaced with `@MockitoBean`.

## Running Tests

```bash
# Run all tests
./mvnw test

# Run only service tests
./mvnw test -Dtest=AnimalServiceTest

# Run only controller tests
./mvnw test -Dtest=AnimalControllerTest

# Run only repository tests
./mvnw test -Dtest=AnimalRepositoryTest

# Run only integration tests
./mvnw test -Dtest=AdoptionIntegrationTest
```

## Project Structure

```
src/main/java/lv/bootcamp/shelter/
├── client/
│   ├── NotificationClient.java          (interface — external seam)
│   └── LoggingNotificationClient.java   (default impl)
├── controller/
│   ├── AnimalController.java            (REST endpoints)
│   └── GlobalExceptionHandler.java      (error responses)
├── dto/
│   ├── AdoptionRequest.java
│   ├── AnimalCreateRequest.java
│   └── AnimalResponse.java
├── model/
│   ├── Animal.java                      (JPA entity)
│   ├── AnimalStatus.java
│   └── AnimalType.java
├── repository/
│   └── AnimalRepository.java            (Spring Data JPA)
├── service/
│   ├── AnimalService.java               (business logic)
│   └── AnimalNotFoundException.java
└── ShelterTestingApplication.java

src/test/java/lv/bootcamp/shelter/
├── controller/
│   └── AnimalControllerTest.java        (@WebMvcTest)
├── repository/
│   └── AnimalRepositoryTest.java        (@DataJpaTest)
├── service/
│   └── AnimalServiceTest.java           (Mockito unit test)
└── AdoptionIntegrationTest.java         (@SpringBootTest)
```

## Prerequisites

- Java 21
- Maven 3.9+

## Teacher's comments
``` text
What should be improved in code quality?
AnimalServiceTest.create_shouldSaveAnimalWithAvailableStatus: you stub save() to return a separately constructed hardcoded Animal. The response therefore comes from the stub rather than proving that the request was mapped correctly, while the captured entity is checked only for status. Return the repository argument with thenAnswer(returnsFirstArg()) (setting its ID in the answer if needed), then validate all request fields on both the saved entity and returned response.
AnimalServiceTest.findById_shouldThrowWhenAnimalNotFound: you verify only that the exception message contains "99". Assert the full expected message, such as "Animal not found: 99", so an unrelated message containing the same number cannot pass.
AnimalPageControllerTest.listAnimals_shouldAddAnimalsToModel: attributeExists("animals") proves only that some value was added under that name. Assert the exact expected list, for example model().attribute("animals", List.of(rex)), so the test catches a controller that places the wrong data in the model.
AnimalRepositoryTest.findByStatus_shouldReturnOnlyMatchingAnimals asserts only .hasSize(2), so it can pass if two wrong animals are returned. findByType_shouldReturnAnimalsOfGivenType can also pass vacuously when the result is empty because it uses only forEach. Assert the expected names or IDs and the expected size so both tests verify the actual matching entities.
AnimalControllerSecurityTest: all three mockMvc.perform(...).andExpect(...) chains are compressed onto one line. Break the request and each expectation onto separate lines so the test flow is easy to scan.

Non-scored observation
AdoptionIntegrationTest.adoptionFlow_shouldPersistStatusAndNotifyExternalSystem verifies the notification using adopted.id() and adopted.name(). Those values come from the same operation being tested, so a consistently wrong response and notification could agree. Use the independently known created animal ID and expected name (created.id() and "Rex") as the verification values.
```
