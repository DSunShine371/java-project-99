package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByAssigneeId(Long id);
    Optional<Task> findByTaskStatusId(Long id);
    Optional<Task> findByIndex(Integer index);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN t.assignee a
        LEFT JOIN t.taskStatus ts
        LEFT JOIN t.labels l
        WHERE (:titleCont IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :titleCont, '%')))
        AND (:assigneeId IS NULL OR a.id = :assigneeId)
        AND (:status IS NULL OR ts.slug = :status)
        AND (:labelId IS NULL OR l.id = :labelId)
    """)
    List<Task> findByFilters(
            @Param("titleCont") String titleCont,
            @Param("assigneeId") Long assigneeId,
            @Param("status") String status,
            @Param("labelId") Long labelId
    );

    @EntityGraph(attributePaths = {"labels", "taskStatus", "assignee"})
    @Override
    Optional<Task> findById(Long id);

    @EntityGraph(attributePaths = {"labels", "taskStatus", "assignee"})
    @Override
    List<Task> findAll();

    boolean existsByLabelsId(Long labelId);
}
