package info.syachiku.blog;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static info.syachiku.blog.VerificationApplication.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = VerificationApplication.class)
@AutoConfigureMockMvc
class ArticleVerificationTest {
    @Autowired MockMvc mockMvc;
    @Autowired Validator validator;

    @Test void legacyGroupSkipsCommonNameConstraint() {
        assertThat(validator.validate(new GroupForm(null, ""), LegacyCreateGroup.class)).isEmpty();
    }

    static Stream<Arguments> groupCases() {
        return Stream.of(
            Arguments.of(CreateGroup.class, null, "Alice", Set.of()),
            Arguments.of(CreateGroup.class, null, "", Set.of("name")),
            Arguments.of(CreateGroup.class, 10L, "Alice", Set.of("id")),
            Arguments.of(CreateGroup.class, 10L, "", Set.of("id", "name")),
            Arguments.of(UpdateGroup.class, 10L, "Alice", Set.of()),
            Arguments.of(UpdateGroup.class, 10L, "", Set.of("name")),
            Arguments.of(UpdateGroup.class, null, "Alice", Set.of("id")),
            Arguments.of(UpdateGroup.class, null, "", Set.of("id", "name"))
        );
    }
    @ParameterizedTest @MethodSource("groupCases")
    void groupAlsoValidatesCommonName(Class<?> group, Long id, String name, Set<String> expected) {
        var actual = validator.validate(new GroupForm(id, name), group).stream()
            .map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);
    }

    @Test void blankNameRendersActualJapaneseMessageInHtml() throws Exception {
        mockMvc.perform(post("/users").locale(Locale.JAPANESE)
                .param("name", "").param("email", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("users/input"))
            .andExpect(model().attributeHasFieldErrors("userForm", "name"))
            .andExpect(content().string(containsString("<p id=\"name-error\">名前を入力してください。</p>")))
            .andExpect(content().string(containsString("name=\"email\" value=\"test@example.com\"")));
    }
    @Test void validNameRedirectsAfterSuccess() throws Exception {
        mockMvc.perform(post("/users").param("name", "Alice").param("email", "test@example.com"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/users"));
    }
    @Test void initialPageHasNoErrorMessage() throws Exception {
        mockMvc.perform(get("/users/new"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("id=\"name-error\""))));
    }

    @Test void absentRequiredParameterIs400() throws Exception {
        var result = mockMvc.perform(get("/orders")).andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(MissingServletRequestParameterException.class);
    }
    @Test void nonNumericPathIs400TypeMismatch() throws Exception {
        var result = mockMvc.perform(get("/orders/abc")).andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);
        var ex = (MethodArgumentTypeMismatchException) result.getResolvedException();
        assertThat(ex.getName()).isEqualTo("orderId");
        assertThat(ex.getValue()).isEqualTo("abc");
    }
    @Test void unparseableJsonIs400BeforeValidation() throws Exception {
        var result = mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":10,\"quantity\":\"二個\"}"))
            .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(HttpMessageNotReadableException.class);
    }
    @Test void readableJsonWithZeroQuantityIs400Validation() throws Exception {
        var result = mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":10,\"quantity\":0}"))
            .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
        var ex = (MethodArgumentNotValidException) result.getResolvedException();
        assertThat(ex.getBindingResult().getFieldErrors()).extracting("field").containsExactly("quantity");
    }
    @Test void correctedRequestsSucceed() throws Exception {
        mockMvc.perform(get("/orders").param("status", "WAITING"))
            .andExpect(status().isOk()).andExpect(content().string("WAITING"));
        mockMvc.perform(get("/orders/10"))
            .andExpect(status().isOk()).andExpect(content().string("10"));
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":10,\"quantity\":2}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2));
    }
}
