package info.syachiku.formkeep;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.validation.Valid;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({FormKeepTest.BrokenController.class, FormKeepTest.MisplacedController.class})
class FormKeepTest {
    @Autowired MockMvc mvc;

    @Controller
    static class BrokenController {
        @PostMapping("/broken/employees")
        public String create(@Valid @ModelAttribute("employeeForm") EmployeeForm form,
                             BindingResult bindingResult, Model model) {
            if (bindingResult.hasErrors()) {
                return "employees/form";
            }
            return "redirect:/employees/complete";
        }
    }

    @Controller
    static class MisplacedController {
        @PostMapping("/misplaced/employees")
        @ResponseBody
        public String create(@Valid @ModelAttribute("employeeForm") EmployeeForm form,
                             Model model, BindingResult bindingResult) {
            return "handler-called";
        }
    }

    private MvcResult invalidPost(String url) throws Exception {
        return mvc.perform(post(url).characterEncoding("UTF-8")
                .param("name", "山田太郎").param("departmentCode", "DEV").param("email", "invalid"))
            .andExpect(status().isOk()).andExpect(view().name("employees/form"))
            .andExpect(model().attributeHasFieldErrors("employeeForm", "email"))
            .andReturn();
    }

    private Document html(MvcResult result, String name) throws Exception {
        String source = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Path dir = Path.of("target", "evidence");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(name + ".html"), source, StandardCharsets.UTF_8);
        return Jsoup.parse(source);
    }

    @Test
    void missingReferenceDataLosesOptionsButKeepsBoundFormAndText() throws Exception {
        MvcResult result = invalidPost("/broken/employees");
        EmployeeForm form = (EmployeeForm) result.getModelAndView().getModel().get("employeeForm");
        assertThat(form.getDepartmentCode()).isEqualTo("DEV");
        assertThat(result.getModelAndView().getModel()).doesNotContainKey("departments");
        Document doc = html(result, "broken-invalid-post");
        assertThat(doc.select("input[name=name]").attr("value")).isEqualTo("山田太郎");
        assertThat(doc.select("input[name=email]").attr("value")).isEqualTo("invalid");
        assertThat(doc.select("#email-error").text()).isEqualTo("メールアドレスの形式を確認してください");
        assertThat(doc.select("select[name=departmentCode] option")).hasSize(1);
        assertThat(doc.select("select[name=departmentCode] option[value=DEV]")).isEmpty();
        assertThat(doc.select("select[name=departmentCode] option[selected]")).isEmpty();
    }

    @Test
    void restoredReferenceDataKeepsTextSelectedValueOptionsAndErrorInRenderedHtml() throws Exception {
        MvcResult result = invalidPost("/employees");
        Document doc = html(result, "fixed-invalid-post");
        assertThat(doc.select("input[name=name]").attr("value")).isEqualTo("山田太郎");
        assertThat(doc.select("input[name=email]").attr("value")).isEqualTo("invalid");
        assertThat(doc.select("#email-error").text()).isEqualTo("メールアドレスの形式を確認してください");
        assertThat(doc.select("select[name=departmentCode] option").eachAttr("value"))
            .containsExactly("", "DEV", "SALES");
        assertThat(doc.select("select[name=departmentCode] option").eachText())
            .containsExactly("選択してください", "開発", "営業");
        assertThat(doc.select("select[name=departmentCode] option[selected]")).hasSize(1);
        assertThat(doc.select("select[name=departmentCode] option[selected]").attr("value")).isEqualTo("DEV");
    }

    @Test
    void initialGetHasEmptyTextAndAllOptions() throws Exception {
        MvcResult result = mvc.perform(get("/employees/new"))
            .andExpect(status().isOk()).andExpect(view().name("employees/form"))
            .andExpect(model().hasNoErrors()).andReturn();
        Document doc = html(result, "initial-get");
        assertThat(doc.select("input[name=name]").attr("value")).isEmpty();
        assertThat(doc.select("input[name=email]").attr("value")).isEmpty();
        assertThat(doc.select("select[name=departmentCode] option").eachAttr("value"))
            .containsExactly("", "DEV", "SALES");
        assertThat(doc.select("#email-error")).isEmpty();
    }

    @Test
    void validPostRedirectsToCompletion() throws Exception {
        mvc.perform(post("/employees").characterEncoding("UTF-8")
                .param("name", "山田太郎").param("departmentCode", "DEV")
                .param("email", "yamada@example.com"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/employees/complete"))
            .andExpect(model().hasNoErrors());
        mvc.perform(get("/employees/complete")).andExpect(status().isOk());
    }

    @Test
    void misplacedBindingResultWithInvalidInputReturns400BeforeHandler() throws Exception {
        MvcResult result = mvc.perform(post("/misplaced/employees")
                .param("name", "山田太郎").param("departmentCode", "DEV").param("email", "invalid"))
            .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
    }

    @Test
    void misplacedBindingResultDoesNotFailForThisValidInput() throws Exception {
        mvc.perform(post("/misplaced/employees")
                .param("name", "山田太郎").param("departmentCode", "DEV").param("email", "yamada@example.com"))
            .andExpect(status().isOk()).andExpect(content().string("handler-called"));
    }
}
