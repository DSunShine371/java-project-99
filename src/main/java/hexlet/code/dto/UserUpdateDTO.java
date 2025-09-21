package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
public class UserUpdateDTO {
    private JsonNullable<@Email(message = "Email must be a valid format")
            String> email;

    private JsonNullable<String> firstName;

    private JsonNullable<String> lastName;

    private JsonNullable<@Size(min = 3, message = "Password must be at least 3 characters long")
            String> password;
}
