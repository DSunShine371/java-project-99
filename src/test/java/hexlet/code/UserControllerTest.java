package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
public class UserControllerTest {

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

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateUser() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("test@example.com");
        userCreateDTO.setFirstName("Test");
        userCreateDTO.setLastName("User");
        userCreateDTO.setPassword("password123");

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(userCreateDTO))
                )
                .andExpect(status().isCreated());

        User createdUser = userRepository.findByEmail("test@example.com").get();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getFirstName()).isEqualTo("Test");
        assertThat(createdUser.getPasswordDigest()).isNotEqualTo("password123");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateUserWithInvalidData() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("invalid-email");
        userCreateDTO.setPassword("12");

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(userCreateDTO))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    public void testGetUsers() throws Exception {
        UserCreateDTO user1 = new UserCreateDTO();
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        userService.create(user1);

        UserCreateDTO user2 = new UserCreateDTO();
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        userService.create(user2);

        mockMvc.perform(
                        get("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "initial@example.com")
    public void testUpdateUser() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("initial@example.com");
        userCreateDTO.setFirstName("Initial");
        userCreateDTO.setPassword("initialpassword");

        User createdUser = userService.create(userCreateDTO);

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFirstName(JsonNullable.of("Updated"));
        userUpdateDTO.setPassword(JsonNullable.of("newpassword123"));

        mockMvc.perform(
                        put("/api/users/" + createdUser.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(userUpdateDTO))
                )
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(createdUser.getId()).get();
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getPasswordDigest()).isNotEqualTo("newpassword123");
    }

    @Test
    @WithMockUser(username = "delete@example.com")
    public void testDeleteUser() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("delete@example.com");
        userCreateDTO.setPassword("password");

        User userToDelete = userService.create(userCreateDTO);

        mockMvc.perform(
                        delete("/api/users/" + userToDelete.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(userToDelete.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    public void testUpdateOtherUserForbidden() throws Exception {
        UserCreateDTO user1 = new UserCreateDTO();
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        User user = userService.create(user1);

        UserCreateDTO user2 = new UserCreateDTO();
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        User otherUser = userService.create(user2);

        UserUpdateDTO updateData = new UserUpdateDTO();
        updateData.setFirstName(JsonNullable.of("NewName"));

        mockMvc.perform(put("/api/users/" + otherUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateData)))
                .andExpect(status().isForbidden());
    }
}
