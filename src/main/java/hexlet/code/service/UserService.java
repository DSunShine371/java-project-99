package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            TaskRepository taskRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO create(UserCreateDTO userData) {
        var user = userMapper.map(userData);
        var hashedPassword = passwordEncoder.encode(userData.getPassword());
        user.setPasswordDigest(hashedPassword);

        return userMapper.map(userRepository.save(user));
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO findById(Long id) {
        return userMapper.map(findUserById(id));
    }

    public UserDTO update(UserUpdateDTO userData, Long id) {
        User user = findUserById(id);

        if (userData.getPassword() != null && userData.getPassword().isPresent()) {
            var newPassword = userData.getPassword().get();
            var hashedPassword = passwordEncoder.encode(newPassword);
            user.setPasswordDigest(hashedPassword);
        }
        userMapper.update(userData, user);

        return userMapper.map(userRepository.save(user));
    }

    public void delete(Long id) {
        if (taskRepository.findByAssigneeId(id).isPresent()) {
            throw new DataIntegrityViolationException("User is assigned to a task and cannot be deleted.");
        }
        userRepository.deleteById(id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    public UserDTO findUserByEmail(String email) {
        return userMapper.map(findByEmail(email));
    }

    public boolean isAdmin(String email)  {
        return findByEmail(email).getIsAdmin();
    }
    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
    }
}
