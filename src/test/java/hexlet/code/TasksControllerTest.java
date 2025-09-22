package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.config.JWTUtils;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
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
public class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private JWTUtils jwtUtils;

    private String token;
    private User testUser;
    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordDigest("password");
        userRepository.save(testUser);
        token = jwtUtils.generateToken(testUser.getEmail());

        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("Draft");
        testTaskStatus.setSlug("draft");
        taskStatusRepository.save(testTaskStatus);
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    public void testCreateTask() throws Exception {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("New Task Title");
        dto.setDescription("Some description");
        dto.setStatus(testTaskStatus.getSlug());
        dto.setAssigneeId(testUser.getId());

        var request = post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        assertThat(taskRepository.count()).isEqualTo(1);
        var task = taskRepository.findAll().get(0);
        assertThat(task.getName()).isEqualTo("New Task Title");
        assertThat(task.getAssignee().getId()).isEqualTo(testUser.getId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    @Test
    public void testUpdateTask() throws Exception {
        var task = new hexlet.code.model.Task();
        task.setName("Initial Title");
        task.setTaskStatus(testTaskStatus);
        task.setAssignee(testUser);
        taskRepository.save(task);

        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setName(JsonNullable.of("Updated Title"));
        dto.setDescription(JsonNullable.of("Updated description"));

        var request = put("/api/tasks/" + task.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTask = taskRepository.findById(task.getId()).get();
        assertThat(updatedTask.getName()).isEqualTo("Updated Title");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated description");
    }

    @Test
    public void testDeleteTask() throws Exception {
        var task = new hexlet.code.model.Task();
        task.setName("To be deleted");
        task.setTaskStatus(testTaskStatus);
        taskRepository.save(task);

        var request = delete("/api/tasks/" + task.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(task.getId())).isFalse();
    }

    @Test
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }
}
