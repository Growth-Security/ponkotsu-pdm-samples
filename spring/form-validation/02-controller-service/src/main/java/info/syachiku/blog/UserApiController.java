package info.syachiku.blog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserApiController {
    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredUser register(@Valid @RequestBody UserRegisterRequest request) {
        long id = userService.register(request.toCommand());
        return new RegisteredUser(id);
    }

    public record UserRegisterRequest(
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Email String email,
            String departmentCode) {
        public UserRegisterCommand toCommand() {
            return new UserRegisterCommand(name, email, departmentCode);
        }
    }

    public record RegisteredUser(long id) {
    }
}
