package hexlet.code.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task implements BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Name cannot be empty.")
    @Size(min = 1, message = "Name must contain at least 1 character.")
    private String name;

    private Integer index;

    private String description;

    @NotNull
    @ManyToOne
    private TaskStatus taskStatus;

    @ManyToOne
    private User assignee;

    @CreatedDate
    private Instant createdAt;
}
