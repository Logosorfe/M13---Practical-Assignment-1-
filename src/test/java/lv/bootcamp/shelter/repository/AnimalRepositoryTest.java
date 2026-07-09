package lv.bootcamp.shelter.repository;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task: Repository tests with @DataJpaTest.
 * <p>
 * Use entityManager.persist() + entityManager.flush() to set up test data.
 * Each test rolls back automatically — no cleanup needed.
 */
@DataJpaTest
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Animal rex;
    private Animal murka;
    private Animal oldJoe;

    @BeforeEach
    void setUp() {
        rex = new Animal(null, "Rex", AnimalType.DOG, "Labrador", 5, "Strong dog", AnimalStatus.AVAILABLE);
        murka = new Animal(null, "Murka", AnimalType.CAT, "Siamese", 3, "Calm cat", AnimalStatus.AVAILABLE);
        oldJoe = new Animal(null, "Old Joe", AnimalType.DOG, "Mixed", 12, "Old dog", AnimalStatus.ADOPTED);
    }

    @Test
    void save_shouldPersistAnimalAndGenerateId() {
        Animal saved = animalRepository.save(rex);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Rex");
    }

    @Test
    void findByStatus_shouldReturnOnlyMatchingAnimals() {
        entityManager.persist(rex);
        entityManager.persist(murka);
        entityManager.persist(oldJoe);
        entityManager.flush();

        List<Animal> available = animalRepository.findByStatus(AnimalStatus.AVAILABLE);

        assertThat(available).hasSize(2);
    }

    @Test
    void findByType_shouldReturnAnimalsOfGivenType() {
        entityManager.persist(rex);
        entityManager.persist(murka);
        entityManager.flush();

        List<Animal> dogs = animalRepository.findByType(AnimalType.DOG);

        dogs.forEach(animal -> assertThat(animal.getType()).isEqualTo(AnimalType.DOG));
    }

    @Test
    void findByNameContainingIgnoreCase_shouldMatchPartialName() {
        Animal rexy = new Animal(null, "Rexy Jr", AnimalType.DOG, "Mixed", 2,
                "Puppy", AnimalStatus.AVAILABLE);
        Animal mia = new Animal(null, "Mia", AnimalType.CAT, "Siamese", 3,
                "Calm cat", AnimalStatus.AVAILABLE);

        entityManager.persist(rex);
        entityManager.persist(mia);
        entityManager.persist(rexy);
        entityManager.flush();

        List<Animal> results = animalRepository.findByNameContainingIgnoreCase("rex");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Animal::getName).containsExactlyInAnyOrder("Rex", "Rexy Jr");
    }
}
