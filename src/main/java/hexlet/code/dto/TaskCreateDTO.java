package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TaskCreateDTO {

    private Integer index;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @NotNull(message = "Title cannot be null.")
    @Size(min = 1, message = "Title must contain at least 1 character.")
    @JsonProperty("title")
    private String name;

    @JsonProperty("content")
    private String description;

    @NotBlank(message = "Status cannot be empty.")
    private String status;

    private Set<Long> taskLabelIds = new HashSet<>();
}
