package com.pm.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Authorize all requests it receives to the auth-service
        // no need to block requests at this level
        // we are only getting requests from API gateway(it is in our control)
        http.authorizeHttpRequests(
                authorize -> authorize.anyRequest().permitAll()
        ).csrf(AbstractHttpConfigurer::disable); // disable cross site request forgery(since our traffic comes from gateway)
        // csrf it used to protect against front-end client requests that have a hacked auth token
        // we do not need this because our auth service is secured by API gateway

        return http.build(); // this will complete the filter chain and
        // configure spring security to be a bit less secure than we need it to be
    }

    @Bean
    // add this bean as a dependency to AuthService(will be injected by spring)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
