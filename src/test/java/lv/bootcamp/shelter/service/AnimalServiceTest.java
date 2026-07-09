package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.client.NotificationClient;
import lv.bootcamp.shelter.dto.AdoptionRequest;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.repository.AnimalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Task: Service-layer tests with Mockito.
 * <p>
 * Use @Mock, @InjectMocks, stubbing, verify(), and ArgumentCaptor.
 * Write Arrange-Act-Assert for each method.
 */
@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AnimalService animalService;

    @Captor
    private ArgumentCaptor<Animal> animalCaptor;

    @Captor
    private ArgumentCaptor<List<Long>> idsCaptor;

    private Animal rex;
    private Animal murka;

    @BeforeEach
    void setUp() {
        rex = new Animal(1L, "Rex", AnimalType.DOG, "Labrador", 5, "Strong dog", AnimalStatus.AVAILABLE);
        murka = new Animal(2L, "Murka", AnimalType.CAT, "Siamese", 3, "Calm cat", AnimalStatus.AVAILABLE);
    }

    @Test
    void create_shouldSaveAnimalWithAvailableStatus() {
        AnimalCreateRequest request = new AnimalCreateRequest("Rex", AnimalType.DOG, "Labrador",
                5, "Strong dog");

        when(animalRepository.save(any())).thenReturn(rex);

        var response = animalService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Rex");
        assertThat(response.status()).isEqualTo(AnimalStatus.AVAILABLE);
        verify(animalRepository).save(animalCaptor.capture());

        Animal captured = animalCaptor.getValue();

        assertThat(captured.getStatus()).isEqualTo(AnimalStatus.AVAILABLE);
    }

    @Test
    void findById_shouldThrowWhenAnimalNotFound() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> animalService.findById(99L))
                .isInstanceOf(AnimalNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void adopt_shouldChangeStatusAndSendNotification() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(rex));
        when(animalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = animalService.adopt(new AdoptionRequest(1L, null,
                "john@example.com"));

        assertThat(response.status()).isEqualTo(AnimalStatus.ADOPTED);
        verify(notificationClient).sendAdoptionNotification(1L, "Rex",
                "john@example.com");
    }

    @Test
    void adopt_shouldThrowWhenAnimalAlreadyAdopted() {
        rex.setStatus(AnimalStatus.ADOPTED);

        when(animalRepository.findById(1L)).thenReturn(Optional.of(rex));
        assertThatThrownBy(() -> animalService.adopt(new AdoptionRequest(1L, null,
                "john@example.com"))).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void reserveMultiple_shouldNotifyWithReservedIds() {
        when(animalRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(rex, murka));
        when(animalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var responses = animalService.reserveMultiple(List.of(1L, 2L));

        assertThat(responses).extracting("status")
                .containsExactly(AnimalStatus.RESERVED, AnimalStatus.RESERVED);
        verify(notificationClient).sendBulkStatusNotification(idsCaptor.capture(), eq("RESERVED"));
        assertThat(idsCaptor.getValue()).containsExactly(1L, 2L);
    }
}
