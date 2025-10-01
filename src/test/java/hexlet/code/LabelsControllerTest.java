package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.config.JWTUtils;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LabelsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private JWTUtils jwtUtils;

    private String token;
    private Label testLabel;

    @BeforeEach
    public void setUp() {
        labelRepository.deleteAll();
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordDigest("password");
        userRepository.save(testUser);
        token = jwtUtils.generateToken(testUser.getEmail());

        testLabel = new Label();
        testLabel.setName("test label");
        labelRepository.save(testLabel);
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    public void testCreateLabel() throws Exception {
        LabelCreateDTO dto = new LabelCreateDTO("bug");

        var request = post("/api/labels")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        assertThat(labelRepository.findByName("bug")).isPresent();
    }

    @Test
    public void testCreateLabelWithInvalidData() throws Exception {
        LabelCreateDTO dto = new LabelCreateDTO("a"); // Имя слишком короткое

        var request = post("/api/labels")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateLabel() throws Exception {
        LabelUpdateDTO dto = new LabelUpdateDTO();
        dto.setName(JsonNullable.of("updated feature"));

        var request = put("/api/labels/" + testLabel.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedLabel = labelRepository.findById(testLabel.getId()).get();
        assertThat(updatedLabel.getName()).isEqualTo("updated feature");
    }

    @Test
    public void testGetAllLabels() throws Exception {
        mockMvc.perform(get("/api/labels").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLabelById() throws Exception {
        mockMvc.perform(get("/api/labels/" + testLabel.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteLabel() throws Exception {
        mockMvc.perform(delete("/api/labels/" + testLabel.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.existsById(testLabel.getId())).isFalse();
    }

    // ⭐ Новый важный тест
    @Test
    public void testDeleteLabelFailsIfAssociatedWithTask() throws Exception {
        // Создаем задачу и связываем ее с нашей тестовой меткой
        TaskStatus status = new TaskStatus();
        status.setName("Draft");
        status.setSlug("draft");
        taskStatusRepository.save(status);

        TaskCreateDTO taskDto = new TaskCreateDTO();
        taskDto.setName("Test Task");
        taskDto.setStatus("draft");
        taskDto.setTaskLabelIds(Set.of(testLabel.getId()));
        taskService.create(taskDto);

        // Пытаемся удалить метку
        mockMvc.perform(delete("/api/labels/" + testLabel.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest()); // Ожидаем ошибку (например, 400)

        // Убеждаемся, что метка не была удалена
        assertThat(labelRepository.existsById(testLabel.getId())).isTrue();
    }

    @Test
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/labels").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
