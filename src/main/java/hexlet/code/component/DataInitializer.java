package hexlet.code.component;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.model.User;
import hexlet.code.service.LabelService;
import hexlet.code.service.SecurityUserDetailsService;
import hexlet.code.service.TaskStatusService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final SecurityUserDetailsService userService;

    @Autowired
    private final TaskStatusService taskStatusService;

    @Autowired
    private final LabelService labelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeAdminUser();
        initializeDefaultStatuses();
        initializeDefaultLabels();
    }

    private void initializeAdminUser() {
        String adminEmail = "hexlet@example.com";
        if (!userService.userExists(adminEmail)) {
            var userData = new User();
            userData.setEmail(adminEmail);
            userData.setFirstName("Admin");
            userData.setLastName("Adminskiy");
            userData.setPasswordDigest("qwerty");
            userService.createUser(userData);
        }
    }

    private void initializeDefaultStatuses() {
        initializeStatus("Draft", "draft");
        initializeStatus("To Review", "to_review");
        initializeStatus("To Be Fixed", "to_be_fixed");
        initializeStatus("To Publish", "to_publish");
        initializeStatus("Published", "published");
    }

    private void initializeStatus(String name, String slug) {
        if (!taskStatusService.taskStatusExists(slug)) {
            taskStatusService.create(new TaskStatusCreateDTO(name, slug));
        }
    }

    private void initializeDefaultLabels() {
        initializeLabels("feature");
        initializeLabels("bug");
    }

    private void initializeLabels(String name) {
        if (!labelService.labelExists(name)) {
            labelService.create(new LabelCreateDTO(name));
        }
    }
}
