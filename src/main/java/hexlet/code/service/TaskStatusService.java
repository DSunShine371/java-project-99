package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    private final TaskRepository taskRepository;

    private final TaskStatusMapper taskStatusMapper;

    public TaskStatusService(
            TaskStatusRepository taskStatusRepository,
            TaskRepository taskRepository,
            TaskStatusMapper taskStatusMapper) {
        this.taskStatusRepository = taskStatusRepository;
        this.taskRepository = taskRepository;
        this.taskStatusMapper = taskStatusMapper;
    }

    public TaskStatusDTO create(TaskStatusCreateDTO taskStatusData) {
        var taskStatus = taskStatusMapper.map(taskStatusData);
        return taskStatusMapper.map(taskStatusRepository.save(taskStatus));
    }

    public List<TaskStatusDTO> findAll() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO findById(Long id) {
        return taskStatusMapper.map(findTaskStatusById(id));
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO taskStatusData, Long id) {
        var taskStatus = findTaskStatusById(id);
        taskStatusMapper.update(taskStatusData, taskStatus);

        return taskStatusMapper.map(taskStatusRepository.save(taskStatus));
    }

    public boolean taskStatusExists(String slug) {
        return taskStatusRepository.findBySlug(slug).isPresent();
    }

    public void delete(Long id) {
        if (taskRepository.findByTaskStatusId(id).isPresent()) {
            throw new DataIntegrityViolationException("Status is used by a task and cannot be deleted.");
        }
        taskStatusRepository.deleteById(id);
    }

    private TaskStatus findTaskStatusById(Long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}
