package info.syachiku.blog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/orders", "/orders/no-binding", "/api/orders")
                        .hasRole("ORDER")
                        .anyRequest().denyAll())
                .csrf(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    // 検証では user() で認証済みユーザーを用意する。実際のログイン処理は対象外。
    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
