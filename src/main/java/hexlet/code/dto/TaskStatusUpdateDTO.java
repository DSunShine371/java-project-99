package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
public class TaskStatusUpdateDTO {
    private JsonNullable<
            @NotBlank(message = "Name cannot be empty.")
            @Size(min = 1, message = "Name must contain at least 1 character.")
            String> name;

    private JsonNullable<
            @NotBlank(message = "Slug cannot be empty.")
            @Size(min = 1, message = "Slug must contain at least 1 character.")
            String> slug;
}
