package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
public class LabelUpdateDTO {
    private JsonNullable<
            @NotBlank(message = "The label name cannot be empty.")
            @Size(min = 3, max = 1000, message = "The label name must be between 3 and 1000 characters long.")
            String> name;
}
