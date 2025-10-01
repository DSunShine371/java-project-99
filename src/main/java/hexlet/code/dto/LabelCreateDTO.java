package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabelCreateDTO {
    @NotBlank(message = "The label name cannot be empty.")
    @Size(min = 3, max = 1000, message = "The label name must be between 3 and 1000 characters long.")
    private String name;
}
