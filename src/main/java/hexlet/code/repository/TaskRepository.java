package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByAssigneeId(Long id);
    Optional<Task> findByTaskStatusId(Long id);
    Optional<Task> findByIndex(Integer index);
}
