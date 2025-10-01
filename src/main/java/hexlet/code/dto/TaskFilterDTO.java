package hexlet.code.dto;

import lombok.Data;

@Data
public class TaskFilterDTO {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;
}
