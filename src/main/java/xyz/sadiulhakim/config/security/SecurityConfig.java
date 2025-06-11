package xyz.sadiulhakim.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    private final UserDetailsService userDetailsService;

    SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain config(HttpSecurity http, DataSource dataSource) throws Exception {

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
                .rememberMe(me -> me.rememberMeParameter("remember-me")
                        .rememberMeCookieName("remember_me")
                        .useSecureCookie(true)
                        .userDetailsService(userDetailsService)
                        .tokenValiditySeconds(60 * 60 * 24 * 5) // 5 Days
                        .tokenRepository(persistentTokenRepository(dataSource))
                )
                .logout(logout -> logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/login_page?logout=true")
                        .permitAll()
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "SESSION", "session", "remember_me")
                )
                .build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
//        repo.setCreateTableOnStartup(true); // Enable only once
        return repo;
    }
}
