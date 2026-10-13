package info.syachiku.blog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest({OrderController.class, OrderApiController.class})
@Import(SecurityConfig.class)
class PublishedSnippetTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean OrderService orderService;

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
}

@Test
void correctedJsonAfter400ReachesService() throws Exception {
    var failed = mockMvc.perform(post("/api/orders")
                    .with(user("operator").roles("ORDER")).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).content("{broken-json}"))
            .andDo(print()).andExpect(status().isBadRequest()).andReturn();
    assertThat(failed.getResolvedException()).isInstanceOf(HttpMessageNotReadableException.class);
    verifyNoInteractions(orderService);

    var fixed = mockMvc.perform(post("/api/orders")
                    .with(user("operator").roles("ORDER")).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderRequest(10L, 2))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2)).andReturn();
    verify(orderService).create(new OrderRequest(10L, 2));
}
}
