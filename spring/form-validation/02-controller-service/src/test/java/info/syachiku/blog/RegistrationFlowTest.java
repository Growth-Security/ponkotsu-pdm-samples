package info.syachiku.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationFlowTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean UserRepository userRepository;

    @Test
    void htmlDuplicateDisplaysErrorAndPreservesInputWithoutSaving() throws Exception {
        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);
        mockMvc.perform(post("/users").param("name", "山田").param("email", "used@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/new"))
                .andExpect(model().attributeHasFieldErrors("form", "email"))
                .andExpect(content().string(containsString("既に使われています")))
                .andExpect(content().string(containsString("value=\"used@example.com\"")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void htmlBlankInputStopsBeforeBusinessCheck() throws Exception {
        mockMvc.perform(post("/users").param("name", "").param("email", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("名前を入力してください")))
                .andExpect(content().string(containsString("メールアドレスを入力してください")));
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void apiDuplicateReturns409WithJsonAndDoesNotSave() throws Exception {
        when(userRepository.existsByEmail("used@example.com")).thenReturn(true);
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"山田\",\"email\":\"used@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("既に使われています"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void apiInvalidEmailReturns400BeforeBusinessCheck() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"山田\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void htmlValidInputBecomesCommandAndRedirects() throws Exception {
        mockMvc.perform(post("/users").param("name", "山田").param("email", "new@example.com")
                        .param("departmentCode", "DEV").param("admin", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/new?registered"));
        ArgumentCaptor<UserRegisterCommand> captured = ArgumentCaptor.forClass(UserRegisterCommand.class);
        verify(userRepository).save(captured.capture());
        assertThat(captured.getValue()).isEqualTo(new UserRegisterCommand("山田", "new@example.com", "DEV"));
    }

    @Test
    void apiValidInputUsesSameServiceAndReturns201() throws Exception {
        when(userRepository.save(any())).thenReturn(42L);
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"山田\",\"email\":\"api@example.com\",\"departmentCode\":\"DEV\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));
        verify(userRepository).save(new UserRegisterCommand("山田", "api@example.com", "DEV"));
    }

    @Test
    void commandIsSnapshotOfValuesNotReferenceToMutableForm() {
        UserRegisterForm form = new UserRegisterForm();
        form.setName("山田");
        form.setEmail("new@example.com");
        form.setDepartmentCode("DEV");
        UserRegisterCommand command = form.toCommand();
        form.setName("変更後");
        assertThat(command).isEqualTo(new UserRegisterCommand("山田", "new@example.com", "DEV"));
    }
}
