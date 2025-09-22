package hexlet.code.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
public class TaskStatusUpdateDTO {
    private JsonNullable<@Size(min = 1, message = "Name must contain at least 1 character.")
            String> name;

    private JsonNullable<
            @Pattern(regexp = "^[a-z0-9_]+$",
                    message = "Slug must consist of lowercase letters, numbers, and hyphens only.")
            @Size(min = 1, message = "Slug must contain at least 1 character.")
            String> slug;
}
