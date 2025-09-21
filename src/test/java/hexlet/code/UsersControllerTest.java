package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.config.JWTUtils;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
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
    private ObjectMapper om;

    @Autowired
    private JWTUtils jwtUtils;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    public void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateUser() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("test@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPassword("password123");

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());

        User createdUser = userRepository.findByEmail("test@example.com").get();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getFirstName()).isEqualTo("Test");
        assertThat(passwordEncoder.matches("password123", createdUser.getPasswordDigest())).isTrue();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateUserWithInvalidData() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail("invalid-email");
        dto.setPassword("12");

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllUsers() throws Exception {
        User testUser = new User();
        testUser.setEmail("user1@example.com");
        testUser.setPasswordDigest(passwordEncoder.encode("password"));
        userRepository.save(testUser);

        String token = jwtUtils.generateToken(testUser.getEmail());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateUser() throws Exception {
        User userToUpdate = new User();
        userToUpdate.setEmail("update-me@example.com");
        userToUpdate.setPasswordDigest(passwordEncoder.encode("initialPassword"));
        userRepository.save(userToUpdate);

        String token = jwtUtils.generateToken(userToUpdate.getEmail());

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName(JsonNullable.of("Updated"));
        dto.setLastName(JsonNullable.of("User"));

        mockMvc.perform(
                        put("/api/users/" + userToUpdate.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(dto))
                )
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(userToUpdate.getId()).get();
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("User");
    }

    @Test
    public void testDeleteUser() throws Exception {
        User userToDelete = new User();
        userToDelete.setEmail("delete-me@example.com");
        userToDelete.setPasswordDigest(passwordEncoder.encode("password"));
        userRepository.save(userToDelete);

        String token = jwtUtils.generateToken(userToDelete.getEmail());

        mockMvc.perform(delete("/api/users/" + userToDelete.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(userToDelete.getId())).isEmpty();
    }

    @Test
    public void testUpdateOtherUserForbidden() throws Exception {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPasswordDigest(passwordEncoder.encode("password"));
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPasswordDigest(passwordEncoder.encode("password"));
        userRepository.save(user2);

        String tokenForUser1 = jwtUtils.generateToken(user1.getEmail());

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName(JsonNullable.of("NewName"));

        mockMvc.perform(put("/api/users/" + user2.getId())
                        .header("Authorization", "Bearer " + tokenForUser1) // <-- Важный момент
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllUsersUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
