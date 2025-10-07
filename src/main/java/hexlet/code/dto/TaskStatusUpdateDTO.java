package hexlet.code.dto;

import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
public class TaskStatusUpdateDTO {
    private JsonNullable<String> name;
    private JsonNullable<String> slug;
}
