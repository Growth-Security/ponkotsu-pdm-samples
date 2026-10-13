package info.syachiku.blog;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserHtmlController {
    private final UserService userService;

    public UserHtmlController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/new")
    public String show(@ModelAttribute("form") UserRegisterForm form) {
        return "users/new";
    }

    @PostMapping("/users")
    public String register(@Valid @ModelAttribute("form") UserRegisterForm form,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "users/new";
        }
        try {
            userService.register(form.toCommand());
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "duplicate", "既に使われています");
            return "users/new";
        }
        return "redirect:/users/new?registered";
    }
}
