package com.empresa.erp.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * API stateless com HTTP Basic. A autorização por perfil fica nos controllers de cada módulo (@PreAuthorize).
 * Para produção, troque por OAuth2 Resource Server (JWT) mantendo os mesmos perfis.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /* Matchers ant explícitos: com o servlet do console H2 registrado, requestMatchers(String...) pode ser ambíguo. */
    private static final RequestMatcher[] ROTAS_PUBLICAS = {
            antMatcher("/actuator/health"),
            antMatcher("/swagger-ui.html"),
            antMatcher("/swagger-ui/**"),
            antMatcher("/v3/api-docs/**"),
            antMatcher("/h2-console/**")
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(SecurityProperties props) {
        List<UserDetails> usuarios = props.usuarios().stream()
                .map(u -> User.withUsername(u.username())
                        .password(u.senha())
                        .roles(u.perfis().toArray(String[]::new))
                        .build())
                .toList();
        return new InMemoryUserDetailsManager(usuarios);
    }
}
