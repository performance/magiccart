package com.oboco.magiccart.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                    .anyRequest().authenticated()
            }
        
        return http.build()
    }
}