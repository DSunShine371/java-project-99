package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.config.JWTUtils;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.LabelService;
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

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private TaskService taskService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelService labelService;
    @Autowired
    private JWTUtils jwtUtils;

    private String token;
    private Task testTask;
    private User testUser;
    private TaskStatus testTaskStatus;
    private Label testLabel1;
    private Label testLabel2;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordDigest("password");
        userRepository.save(testUser);
        token = jwtUtils.generateToken(testUser.getEmail());

        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("test slug");
        testTaskStatus.setSlug("test_slug");
        taskStatusRepository.save(testTaskStatus);

        testLabel1 = new Label();
        testLabel1.setName("test label 1");
        labelRepository.save(testLabel1);

        testLabel2 = new Label();
        testLabel2.setName("test label 2");
        labelRepository.save(testLabel2);

        testTask = new Task();
        testTask.setName("Test Task");
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        testTask.setDescription("For Tests");
        taskRepository.save(testTask);
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
    }

    @Test
    public void testCreateTask() throws Exception {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("New Task Title");
        dto.setDescription("Some description");
        dto.setStatus(testTaskStatus.getSlug());
        dto.setAssigneeId(testUser.getId());
        dto.setTaskLabelIds(Set.of(testLabel1.getId(), testLabel2.getId()));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(dto.getName()).get();
        assertThat(task.getAssignee().getId()).isEqualTo(testUser.getId());
        assertThat(task.getTaskStatus().getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    @Test
    public void testGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetTaskById() throws Exception {
        mockMvc.perform(get("/api/tasks/"
                        + testTask.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateTask() throws Exception {
        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setName(JsonNullable.of("Updated Title"));
        dto.setDescription(JsonNullable.of("Updated description"));
        dto.setTaskLabelIds(JsonNullable.of(Set.of(testLabel2.getId())));
        dto.setStatus(JsonNullable.of(testTaskStatus.getSlug()));

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        var updatedTask = taskRepository.findById(testTask.getId()).get();
        assertThat(updatedTask.getName()).isEqualTo("Updated Title");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated description");
        assertThat(updatedTask.getTaskStatus()).isEqualTo(testTaskStatus);
        assertThat(updatedTask.getLabels().size()).isEqualTo(1);
        assertThat(updatedTask.getLabels().contains(testLabel2));
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
    public void testTaskMapper() {
        Task task = new Task();
        task.setId(1L);
        task.setName("Test Task");
        task.setDescription("Test Description");

        TaskStatus status = new TaskStatus();
        status.setSlug("draft");
        task.setTaskStatus(status);

        Set<Label> labels = new HashSet<>();

        Label label1 = new Label();
        label1.setId(1L);
        label1.setName("bug");
        labels.add(label1);

        Label label2 = new Label();
        label2.setId(2L);
        label2.setName("feature");
        labels.add(label2);

        task.setLabels(labels);

        TaskDTO dto = taskMapper.map(task);

        System.out.println("Mapped DTO: " + dto);
        System.out.println("Mapped labels: " + dto.getTaskLabelIds());

        assertThat(dto.getTaskLabelIds()).isNotEmpty();
        assertThat(dto.getTaskLabelIds()).contains(1L, 2L);
    }

    @Test
    public void testFilterTasksByTitle() throws Exception {
        Task task1 = new Task();
        task1.setName("Create new feature");
        task1.setTaskStatus(testTaskStatus);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setName("Fix bug in controller");
        task2.setTaskStatus(testTaskStatus);
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks?titleCont=feature")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Create new feature"));
    }

    @Test
    public void testFilterTasksByAssignee() throws Exception {
        User user = new User();
        user.setEmail("user@@example.com");
        user.setPasswordDigest("1234");
        userRepository.save(user);

        Task task = new Task();
        task.setName("Assigned task");
        task.setTaskStatus(testTaskStatus);
        task.setAssignee(user);
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks?assigneeId=" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].assignee_id").value(user.getId()));
    }

    @Test
    public void testFilterTasksByStatus() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("for filter");
        status.setSlug("for_filter");
        taskStatusRepository.save(status);

        Task task = new Task();
        task.setName("Task with status");
        task.setTaskStatus(status);
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks?status=" + status.getSlug())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value(status.getSlug()));
    }

    @Test
    public void testFilterTasksByLabel() throws Exception {
        Task task = new Task();
        task.setName("Task with label");
        task.setTaskStatus(testTaskStatus);
        task.setLabels(Set.of(testLabel1));
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks?labelId=" + testLabel1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task with label"));
    }

    @Test
    public void testFilterTasksWithMultipleFilters() throws Exception {
        Task task = new Task();
        task.setName("Complex filter task");
        task.setTaskStatus(testTaskStatus);
        task.setAssignee(testUser);
        task.setLabels(Set.of(testLabel1));
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks?titleCont=complex&assigneeId="
                        + testUser.getId() + "&status=" + testTaskStatus.getSlug() + "&labelId=" + testLabel1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Complex filter task"));
    }

    @Test
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }
}
