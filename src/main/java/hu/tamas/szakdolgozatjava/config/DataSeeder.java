package hu.tamas.szakdolgozatjava.config;

import hu.tamas.szakdolgozatjava.model.Role;
import hu.tamas.szakdolgozatjava.model.User;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User u = new User();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin123"));
                u.setRoles(Set.of(Role.ADMIN));
                userRepository.save(u);
            }
            if (!userRepository.existsByUsername("szervezo")) {
                User u = new User();
                u.setUsername("szervezo");
                u.setPassword(encoder.encode("szervezo123"));
                u.setRoles(Set.of(Role.SZERVEZO));
                userRepository.save(u);
            }
            if (!userRepository.existsByUsername("lakos")) {
                User u = new User();
                u.setUsername("lakos");
                u.setPassword(encoder.encode("lakos123"));
                u.setRoles(Set.of(Role.LAKOS));
                userRepository.save(u);
            }
        };
    }
}
