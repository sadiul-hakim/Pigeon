package xyz.sadiulhakim.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    private final UserDetailsService userDetailsService;

    SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain config(HttpSecurity http) throws Exception {

        String[] permittedEndpoints = {
                "/css/**",
                "/js/**",
                "/fonts/**",
                "/images/**",
                "/",
                "/register_page",
                "/register"
        };
        return http
                .authorizeHttpRequests(auth -> auth.requestMatchers(permittedEndpoints).permitAll())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .userDetailsService(userDetailsService)
                .formLogin(login -> login
                        .loginPage("/login_page")
                        .loginProcessingUrl("/login")
                        .failureForwardUrl("/login_page?error=true")
                        .permitAll()
                        .defaultSuccessUrl("/chat", true)
                )
                .logout(logout -> logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/login_page?logout=true")
                        .permitAll()
                )
                .build();
    }
}
