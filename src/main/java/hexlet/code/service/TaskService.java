package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskMapper taskMapper;

    public List<TaskDTO> findAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO findById(Long id) {
        Task task = findTaskById(id);
        return taskMapper.map(task);
    }

    public TaskDTO create(TaskCreateDTO taskData) {
        Task task = taskMapper.map(taskData);

        TaskStatus status = taskStatusRepository.findBySlug(taskData.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with slug "
                        + taskData.getStatus() + " not found"));
        task.setTaskStatus(status);

        if (taskData.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskData.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee with id "
                            + taskData.getAssigneeId() + " not found"));
            task.setAssignee(assignee);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        Task task = findTaskById(id);
        taskMapper.update(taskData, task);

        if (taskData.getStatus() != null && taskData.getStatus().isPresent()) {
            TaskStatus status = taskStatusRepository.findBySlug(taskData.getStatus().get())
                    .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with slug "
                            + taskData.getStatus().get() + " not found"));
            task.setTaskStatus(status);
        }

        if (taskData.getAssigneeId() != null && taskData.getAssigneeId().isPresent()) {
            Long assigneeId = taskData.getAssigneeId().get();

            if (assigneeId == null) {
                task.setAssignee(null);
            } else {
                User assignee = userRepository.findById(assigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Assignee with id "
                                + assigneeId + " not found"));
                task.setAssignee(assignee);
            }
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }
}
