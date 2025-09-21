package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        userRepository.deleteById(id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}
