package info.syachiku.blog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class VerificationApplication {
    public interface CreateGroup extends Default {}
    public interface UpdateGroup extends Default {}
    public interface LegacyCreateGroup {}

    public static class GroupForm {
        @Null(groups = {CreateGroup.class, LegacyCreateGroup.class})
        @NotNull(groups = UpdateGroup.class)
        private final Long id;
        @NotBlank
        private final String name;
        public GroupForm(Long id, String name) { this.id = id; this.name = name; }
    }

    public static class UserForm {
        @NotBlank(message = "{user.name.required}")
        @Size(max = 50, message = "{user.name.max}")
        private String name;
        @Email(message = "{user.email.invalid}")
        private String email;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @Controller
    public static class UserController {
        @GetMapping("/users/new")
        public String input(@ModelAttribute("userForm") UserForm form) {
            return "users/input";
        }
        @PostMapping("/users")
        public String create(@Valid @ModelAttribute("userForm") UserForm form,
                             BindingResult bindingResult) {
            if (bindingResult.hasErrors()) {
                return "users/input";
            }
            return "redirect:/users";
        }
    }

    public record OrderRequest(@NotNull Long customerId, @Min(1) Integer quantity) {}

    @RestController
    public static class OrderController {
        @GetMapping("/orders")
        public String list(@RequestParam String status) { return status; }
        @GetMapping("/orders/{orderId}")
        public long detail(@PathVariable long orderId) { return orderId; }
        @PostMapping("/api/orders")
        public OrderRequest create(@Valid @RequestBody OrderRequest request) { return request; }
    }
}
