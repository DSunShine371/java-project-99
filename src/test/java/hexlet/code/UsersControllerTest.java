package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.config.JWTUtils;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private String adminToken;
    private User testUser;
    private User user2;
    private User admin;
    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordDigest(passwordEncoder.encode("password1234"));
        userRepository.save(testUser);
        token = jwtUtils.generateToken(testUser.getEmail());

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPasswordDigest(passwordEncoder.encode("password"));
        userRepository.save(user2);

        admin = new User();
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setPasswordDigest(passwordEncoder.encode("admin12345"));
        admin.setAdmin(true);
        userRepository.save(admin);
        adminToken = jwtUtils.generateToken(admin.getEmail());

        testTaskStatus = new TaskStatus();
        testTaskStatus.setName("test slug");
        testTaskStatus.setSlug("test_slug");
        taskStatusRepository.save(testTaskStatus);
    }

    @AfterEach
    public void cleanUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("new-test@example.com");
        dto.setFirstName("New");
        dto.setLastName("User");
        dto.setPassword("newPassword");

        mockMvc.perform(post("/api/users").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        User createdUser = userRepository.findByEmail("new-test@example.com").get();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getFirstName()).isEqualTo("New");
        assertThat(passwordEncoder.matches("newPassword", createdUser.getPasswordDigest())).isTrue();
    }

    @Test
    public void testCreateUserWithInvalidData() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("invalid-email");
        dto.setPassword("12");

        mockMvc.perform(post("/api/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName(JsonNullable.of("Updated"));
        dto.setLastName(JsonNullable.of("NewUser"));

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("NewUser");
    }

    @Test
    public void testUpdateUserWithPassword() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPassword(JsonNullable.of("updatedPassword"));

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertThat(passwordEncoder.matches("updatedPassword", updatedUser.getPasswordDigest())).isTrue();
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @Test
    public void testUpdateOtherUserForbidden() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName(JsonNullable.of("NewName"));

        mockMvc.perform(put("/api/users/" + user2.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteOtherUserForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/" + user2.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateOtherUserAsAdmin() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName(JsonNullable.of("UpdatedByAdmin"));

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedByAdmin");
    }

    @Test
    public void testDeleteOtherUserAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + user2.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteStatusFailsIfAssociatedWithTask() throws Exception {
        Task testTask = new Task();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testTaskStatus);
        testTask.setName("Title");
        taskRepository.save(testTask);

        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findById(testUser.getId())).isPresent();
    }

    @Test
    public void testGetAllUsersUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
