package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.config.JWTUtils;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskStatusesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private JWTUtils jwtUtils;

    private String token;
    private TaskStatus testTaskStatus;
    private User testUser;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setEmail("test-user@example.com");
        user.setPasswordDigest("password123");
        testUser = userRepository.save(user);
        token = jwtUtils.generateToken(user.getEmail());

        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("In Progress");
        testTaskStatus.setSlug("in-progress");
        taskStatusRepository.save(testTaskStatus);
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testGetAllStatuses() throws Exception {
        mockMvc.perform(get("/api/task_statuses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetStatusById() throws Exception {
        mockMvc.perform(get("/api/task_statuses/" + testTaskStatus.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateStatus() throws Exception {
        TaskStatusCreateDTO dto = new TaskStatusCreateDTO();
        dto.setName("New Status");
        dto.setSlug("new_status");

        var request = post("/api/task_statuses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        TaskStatus savedStatus = taskStatusRepository.findBySlug("new_status").get();
        assertThat(savedStatus).isNotNull();
        assertThat(savedStatus.getName()).isEqualTo("New Status");
    }

    @Test
    public void testUpdateStatus() throws Exception {
        TaskStatusUpdateDTO dto = new TaskStatusUpdateDTO();
        dto.setName(JsonNullable.of("Updated Name"));
        dto.setSlug(JsonNullable.of("updated_slug"));

        var request = put("/api/task_statuses/" + testTaskStatus.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        TaskStatus updatedStatus = taskStatusRepository.findById(testTaskStatus.getId()).get();
        assertThat(updatedStatus.getName()).isEqualTo("Updated Name");
        assertThat(updatedStatus.getSlug()).isEqualTo("updated_slug");
    }

    @Test
    public void testDeleteStatus() throws Exception {
        var request = delete("/api/task_statuses/" + testTaskStatus.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(testTaskStatus.getId())).isEmpty();
    }

    @Test
    public void testDeleteStatusFailsIfAssociatedWithTask() throws Exception {
        Task task = new Task();
        task.setAssignee(testUser);
        task.setName("Test Task");
        task.setTaskStatus(testTaskStatus);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/task_statuses/"
                        + testTaskStatus.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        assertThat(taskStatusRepository.existsById(testTaskStatus.getId())).isTrue();
    }

    @Test
    public void testAccessDeniedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());

        TaskStatusCreateDTO dto = new TaskStatusCreateDTO();
        dto.setName("Some Status");
        dto.setSlug("some_status");
        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
