package xyz.sadiulhakim.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
class AppConfig implements WebMvcConfigurer {

    @Bean
    ObjectMapper jsonMapper() {
        return new ObjectMapper();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/picture/**")
                .addResourceLocations("file:F:/MvcChat/picture/");
    }
}
