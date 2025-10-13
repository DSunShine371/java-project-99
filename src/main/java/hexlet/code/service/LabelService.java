package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LabelService {

    private final LabelRepository labelRepository;

    private final TaskRepository taskRepository;

    private final LabelMapper labelMapper;

    public LabelService(
            LabelRepository labelRepository,
            TaskRepository taskRepository,
            LabelMapper labelMapper) {
        this.labelRepository = labelRepository;
        this.taskRepository = taskRepository;
        this.labelMapper = labelMapper;
    }

    public List<LabelDTO> findAll() {
        return labelRepository.findAll().stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO findById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        return labelMapper.map(label);
    }

    public LabelDTO create(LabelCreateDTO labelData) {
        Label label = labelMapper.map(labelData);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO update(LabelUpdateDTO labelData, Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        labelMapper.update(labelData, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public boolean labelExists(String name) {
        return labelRepository.findByName(name).isPresent();
    }

    public void delete(Long id) {
        if (taskRepository.existsByLabelsId(id)) {
            throw new DataIntegrityViolationException("Label is associated with a task and cannot be deleted.");
        }
        labelRepository.deleteById(id);
    }
}
