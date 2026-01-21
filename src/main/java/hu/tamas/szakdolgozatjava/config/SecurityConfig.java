package hu.tamas.szakdolgozatjava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // H2 console (dev)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // szép 403 oldal
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))

                .authorizeHttpRequests(auth -> auth
                        // publikus oldalak
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**"
                        ).permitAll()

                        .requestMatchers("/h2-console/**").permitAll()

                        // eseménylista publikus
                        .requestMatchers("/events").permitAll()

                        // esemény létrehozás: csak SZERVEZO / ADMIN
                        .requestMatchers("/events/new")
                        .hasAnyRole("SZERVEZO", "ADMIN")

                        /*
                         * FONTOS:
                         * edit / update / delete NEM role alapján dől el,
                         * hanem @PreAuthorize + owner check alapján
                         */
                        .requestMatchers(
                                "/events/*/edit",
                                "/events/*/delete",
                                "/events/*"
                        ).authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // minden más csak belépve
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/events", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
