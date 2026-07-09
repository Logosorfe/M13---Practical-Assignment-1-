package lv.bootcamp.shelter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.service.AnimalNotFoundException;
import lv.bootcamp.shelter.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Task: REST controller tests with MockMvc and @WebMvcTest.
 *
 * Stub the service with @MockitoBean. Use mockMvc.perform() to make requests
 * and chain .andExpect() calls to verify status, JSON content, and error responses.
 */
@WebMvcTest(AnimalController.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnimalService animalService;

    @Test
    void findAll_shouldReturnListOfAnimals() throws Exception {
        List<AnimalResponse> responses = List.of(
                new AnimalResponse(1L, "Rex", AnimalType.DOG, "Labrador", 5,
                        "Friendly", AnimalStatus.AVAILABLE),
                new AnimalResponse(2L, "Murka", AnimalType.CAT, "Siamese", 3,
                        "Calm", AnimalStatus.AVAILABLE)
        );

        when(animalService.findAll()).thenReturn(responses);
        mockMvc.perform(get("/api/animals")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Rex"))
                .andExpect(jsonPath("$[1].name").value("Murka"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(animalService.findById(99L)).thenThrow(new AnimalNotFoundException(99L));
        mockMvc.perform(get("/api/animals/99").with(user("user").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithCreatedAnimal() throws Exception {
        AnimalResponse response = new AnimalResponse(
                1L, "Rex", AnimalType.DOG, "Labrador", 5, "Strong dog",
                AnimalStatus.AVAILABLE
        );

        when(animalService.create(any())).thenReturn(response);

        AnimalCreateRequest request = new AnimalCreateRequest(
                "Rex",
                AnimalType.DOG,
                "Labrador",
                5,
                "Strong dog"
        );

        mockMvc.perform(post("/api/animals")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rex"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        String json = """
            {
              "name": "",
              "type": "DOG",
              "breed": "Labrador",
              "age": 5,
              "description": "Strong dog"
            }
            """;

        mockMvc.perform(post("/api/animals")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenTypeIsNull() throws Exception {
        String json = """
            {
              "name": "Rex",
              "type": null,
              "breed": "Labrador",
              "age": 5,
              "description": "Strong dog"
            }
            """;

        mockMvc.perform(post("/api/animals")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
