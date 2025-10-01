package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Data
public class TaskUpdateDTO {

    private JsonNullable<Integer> index;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    @JsonProperty("title")
    private JsonNullable<
            @NotNull(message = "Title cannot be null.")
            @Size(min = 1, message = "Title must contain at least 1 character.")
            String> name;

    @JsonProperty("content")
    private JsonNullable<String> description;

    private JsonNullable<String> status;

    private JsonNullable<Set<Long>> taskLabelIds;
}
