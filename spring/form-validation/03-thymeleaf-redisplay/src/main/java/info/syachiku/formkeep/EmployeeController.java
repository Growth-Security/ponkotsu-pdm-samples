package info.syachiku.formkeep;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    private List<DepartmentView> departments() {
        return List.of(new DepartmentView("DEV", "開発"),
                       new DepartmentView("SALES", "営業"));
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeForm());
        model.addAttribute("departments", departments());
        return "employees/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departments());
            return "employees/form";
        }
        // 再表示の検証用サンプルのため、DBへの登録は省略しています。
        return "redirect:/employees/complete";
    }

    @GetMapping("/complete")
    @ResponseBody
    public String complete() {
        return "入力チェックを通過しました（DBへの登録は行っていません）";
    }
}
