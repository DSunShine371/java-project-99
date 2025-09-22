package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class TaskDTO {
    private Long id;
    private Integer index;
    private Instant createdAt;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @JsonProperty("title")
    private String name;

    @JsonProperty("content")
    private String description;

    private String status;
}
