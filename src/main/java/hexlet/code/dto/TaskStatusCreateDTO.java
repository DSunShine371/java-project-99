package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskStatusCreateDTO {
    @NotBlank(message = "Name cannot be empty.")
    @Size(min = 1, message = "Name must contain at least 1 character.")
    private String name;

    @NotBlank(message = "Slug cannot be empty.")
    @Size(min = 1, message = "Slug must contain at least 1 character.")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Slug must consist of lowercase letters, numbers, and hyphens only.")
    private String slug;
}
