package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskFilterDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TaskStatusRepository taskStatusRepository;

    private final LabelRepository labelRepository;

    private final TaskMapper taskMapper;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskStatusRepository taskStatusRepository,
            LabelRepository labelRepository,
            TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.labelRepository = labelRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskDTO> findAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::map)
                .toList();
    }

    public List<TaskDTO> findByFilters(TaskFilterDTO filters) {
        List<Task> tasks;

        tasks = taskRepository.findByFilters(
                filters.getTitleCont(),
                filters.getAssigneeId(),
                filters.getStatus(),
                filters.getLabelId()
        );

        return tasks.stream()
                .map(taskMapper::map)
                .toList();

    }

    public TaskDTO findById(Long id) {
        return taskMapper.map(findTaskById(id));
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

        if (taskData.getTaskLabelIds() != null && !taskData.getTaskLabelIds().isEmpty()) {
            Set<Label> labels = new HashSet<>();
            for (Long labelId : taskData.getTaskLabelIds()) {
                Label label = labelRepository.findById(labelId)
                        .orElseThrow(() -> new ResourceNotFoundException("Label with id " + labelId + " not found"));
                labels.add(label);
            }
            task.setLabels(labels);
        }

        Task savedTask = taskRepository.save(task);

        return taskMapper.map(taskRepository.findById(savedTask.getId()).get());
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

        if (taskData.getTaskLabelIds() != null) {
            Set<Label> labels = new HashSet<>();

            if (taskData.getTaskLabelIds().isPresent()) {
                Set<Long> labelIds = taskData.getTaskLabelIds().get();
                for (Long labelId : labelIds) {
                    Label label = labelRepository.findById(labelId)
                            .orElseThrow(() -> new ResourceNotFoundException("Label with id "
                                    + labelId + " not found"));
                    labels.add(label);
                }
            }
            task.setLabels(labels);
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.map(taskRepository.findById(updatedTask.getId()).get());
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }
}
