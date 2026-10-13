package info.syachiku.blog;

import java.util.LinkedHashMap;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest({OrderController.class, OrderApiController.class})
@Import(SecurityConfig.class)
class MockMvcPostTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired FilterChainProxy springSecurityFilterChain;
    @MockitoBean OrderService orderService;

    @Test
    void securityFilterChainActuallyContainsCsrfFilter() {
        assertThat(springSecurityFilterChain.getFilters("/orders")).anyMatch(CsrfFilter.class::isInstance);
        assertThat(springSecurityFilterChain.getFilters("/api/orders")).anyMatch(CsrfFilter.class::isInstance);
    }

    static Stream<Arguments> securityCases() {
        return Stream.of(
                Arguments.of("csrf_missing", "ORDER", "missing", 403),
                Arguments.of("csrf_invalid", "ORDER", "invalid", 403),
                Arguments.of("anonymous_with_csrf", null, "valid", 401),
                Arguments.of("anonymous_without_csrf", null, "missing", 403),
                Arguments.of("wrong_role", "READER", "valid", 403));
    }

    @ParameterizedTest @MethodSource("securityCases")
    void securityRejectsBeforeController(String name, String role, String csrfState, int status) throws Exception {
        var request = post("/orders").param("customerId", "10").param("quantity", "2");
        if (role != null) request.with(user("operator").roles(role));
        if (csrfState.equals("valid")) request.with(csrf());
        if (csrfState.equals("invalid")) request.with(csrf().useInvalidToken());
        var result = mockMvc.perform(request).andExpect(status().is(status)).andReturn();
        assertThat(result.getHandler()).isNull();
        assertThat(result.getResolvedException()).isNull();
        verifyNoInteractions(orderService);
        observe(name, result);
    }

    @Test
    void validFormRedirectsAndCallsService() throws Exception {
        var result = mockMvc.perform(post("/orders")
                        .with(user("operator").roles("ORDER"))
                        .with(csrf())
                        .param("customerId", "10").param("quantity", "2"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/orders")).andReturn();
        verify(orderService).create(new OrderRequest(10L, 2));
        observe("form_valid", result);
    }

    @Test
    void formTypeErrorWithBindingResultReturns200AndFieldError() throws Exception {
        var result = mockMvc.perform(post("/orders")
                        .with(user("operator").roles("ORDER"))
                        .with(csrf())
                        .param("customerId", "10").param("quantity", "二個"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/input"))
                .andExpect(model().attributeHasFieldErrorCode("orderForm", "quantity", "typeMismatch"))
                .andReturn();
        verifyNoInteractions(orderService);
        observe("form_type_error_with_binding", result);
    }

    static Stream<Arguments> invalidFormWithBinding() {
        return Stream.of(
                Arguments.of("form_validation_with_binding", "quantity", "0", "Min"),
                Arguments.of("form_wrong_parameter", "count", "2", "NotNull"));
    }

    @ParameterizedTest @MethodSource("invalidFormWithBinding")
    void formErrorsWithBindingReturn200(String name, String param, String value, String error) throws Exception {
        var result = mockMvc.perform(post("/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .param("customerId", "10").param(param, value))
                .andExpect(status().isOk()).andExpect(view().name("orders/input"))
                .andExpect(model().attributeHasFieldErrorCode("orderForm", "quantity", error)).andReturn();
        verifyNoInteractions(orderService);
        observe(name, result);
    }

    static Stream<Arguments> invalidFormsWithoutBinding() {
        return Stream.of(
                Arguments.of("form_type_without_binding", "二個"),
                Arguments.of("form_validation_without_binding", "0"));
    }

    @ParameterizedTest @MethodSource("invalidFormsWithoutBinding")
    void noBindingResultMakesInputErrors400(String name, String quantity) throws Exception {
        var result = mockMvc.perform(post("/orders/no-binding")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .param("customerId", "10").param("quantity", quantity))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
        verifyNoInteractions(orderService);
        observe(name, result);
    }

    static Stream<Arguments> invalidJson() {
        return Stream.of(
                Arguments.of("json_broken", "{broken-json}", HttpMessageNotReadableException.class),
                Arguments.of("json_wrong_type", "{\"customerId\":10,\"quantity\":\"二個\"}", HttpMessageNotReadableException.class),
                Arguments.of("json_validation", "{\"customerId\":10,\"quantity\":0}", MethodArgumentNotValidException.class),
                Arguments.of("json_missing_required", "{\"customerId\":10}", MethodArgumentNotValidException.class));
    }

    @ParameterizedTest @MethodSource("invalidJson")
    void invalidJsonIs400AfterSecurityPasses(String name, String json, Class<?> exceptionType) throws Exception {
        var result = mockMvc.perform(post("/api/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(exceptionType);
        verifyNoInteractions(orderService);
        observe(name, result);
    }

    @Test
    void brokenJsonWithoutCsrfStillStopsAt403() throws Exception {
        var result = mockMvc.perform(post("/api/orders")
                        .with(user("operator").roles("ORDER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{broken-json}"))
                .andExpect(status().isForbidden()).andReturn();
        assertThat(result.getHandler()).isNull();
        assertThat(result.getResolvedException()).isNull();
        verifyNoInteractions(orderService);
        observe("broken_json_without_csrf", result);
    }

    @Test
    void formParamsDoNotSupplyAJsonRequestBody() throws Exception {
        var result = mockMvc.perform(post("/api/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("customerId", "10").param("quantity", "2"))
                .andExpect(status().isBadRequest()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(HttpMessageNotReadableException.class);
        verifyNoInteractions(orderService);
        observe("json_body_absent_params_only", result);
    }

    @Test
    void wrongContentTypeIs415Not400() throws Exception {
        var result = mockMvc.perform(post("/api/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{\"customerId\":10,\"quantity\":2}"))
                .andExpect(status().isUnsupportedMediaType()).andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(HttpMediaTypeNotSupportedException.class);
        verifyNoInteractions(orderService);
        observe("json_wrong_content_type", result);
    }

    @Test
    void validJsonReturns200AndCallsService() throws Exception {
        OrderRequest request = new OrderRequest(10L, 2);
        var result = mockMvc.perform(post("/api/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(10))
                .andExpect(jsonPath("$.quantity").value(2)).andReturn();
        verify(orderService).create(request);
        observe("json_valid", result);
    }

    @Test
    void correctedJsonAfter400ReachesService() throws Exception {
        var failed = mockMvc.perform(post("/api/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{broken-json}"))
                .andDo(print()).andExpect(status().isBadRequest()).andReturn();
        assertThat(failed.getResolvedException()).isInstanceOf(HttpMessageNotReadableException.class);
        verifyNoInteractions(orderService);
        observe("json_before_repair", failed);

        var fixed = mockMvc.perform(post("/api/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest(10L, 2))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2)).andReturn();
        verify(orderService).create(new OrderRequest(10L, 2));
        observe("json_after_repair", fixed);
    }

    @Test
    void addingCsrfFixesTheAuthenticatedFormPost() throws Exception {
        // 失敗を確認：利用者と入力値は正しくても、CSRFがないと403。
        mockMvc.perform(post("/orders")
                        .with(user("operator").roles("ORDER"))
                        .param("customerId", "10").param("quantity", "2"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(orderService);

        // 修正を確認：同じリクエストへ有効なCSRFを足す。
        mockMvc.perform(post("/orders")
                        .with(user("operator").roles("ORDER")).with(csrf())
                        .param("customerId", "10").param("quantity", "2"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/orders"));
        verify(orderService).create(new OrderRequest(10L, 2));
    }

    @Test
    void csrfDoesNotReplaceAuthenticationOrPermission() throws Exception {
        // CSRFだけでは認証されない。このHTTP Basic設定では401。
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("customerId", "10").param("quantity", "2"))
                .andExpect(status().isUnauthorized());

        // 認証済みでも、必要なORDER権限がなければ403。
        mockMvc.perform(post("/orders")
                        .with(user("reader").roles("READER")).with(csrf())
                        .param("customerId", "10").param("quantity", "2"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(orderService);
    }

    private void observe(String name, MvcResult result) throws Exception {
        var row = new LinkedHashMap<String, Object>();
        row.put("case", name);
        row.put("status", result.getResponse().getStatus());
        row.put("handlerPresent", result.getHandler() != null);
        row.put("resolvedException", result.getResolvedException() == null ? null : result.getResolvedException().getClass().getSimpleName());
        row.put("serviceInvocations", mockingDetails(orderService).getInvocations().size());
        if (result.getModelAndView() != null) {
            row.put("view", result.getModelAndView().getViewName());
            Object binding = result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.orderForm");
            if (binding instanceof org.springframework.validation.BindingResult errors) {
                row.put("fieldErrors", errors.getFieldErrors().stream().map(e -> e.getField() + ":" + e.getCode()).toList());
            }
        }
        System.out.println("OBSERVATION " + objectMapper.writeValueAsString(row));
    }
}
