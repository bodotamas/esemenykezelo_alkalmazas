package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.dto.RegisterForm;
import hu.tamas.szakdolgozatjava.model.Role;
import hu.tamas.szakdolgozatjava.model.User;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        RegisterForm form = new RegisterForm();
        form.setRole("LAKOS");
        model.addAttribute("form", form);

        model.addAttribute("roleOptions", new String[]{"LAKOS", "SZERVEZO"});
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("form") RegisterForm form, Model model) {

        model.addAttribute("roleOptions", new String[]{"LAKOS", "SZERVEZO"});

        if (form.getPassword() == null || !form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "A két jelszó nem egyezik.");
            return "auth/register";
        }

        if (userRepository.existsByUsername(form.getUsername())) {
            model.addAttribute("error", "A felhasználónév már foglalt.");
            return "auth/register";
        }

        if (form.getEmail() != null && !form.getEmail().isBlank() && userRepository.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "Ezzel az email címmel már van fiók.");
            return "auth/register";
        }

        Role selectedRole = Role.LAKOS;
        if ("SZERVEZO".equalsIgnoreCase(form.getRole())) {
            selectedRole = Role.SZERVEZO;
        }

        User u = new User();
        u.setUsername(form.getUsername());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.setRoles(Set.of(selectedRole));

        u.setEmail(form.getEmail());
        u.setFirstName(form.getFirstName());
        u.setLastName(form.getLastName());
        u.setHobby(form.getHobby());

        if (form.getBirthDate() != null && !form.getBirthDate().isBlank()) {
            u.setBirthDate(LocalDate.parse(form.getBirthDate()));
        }

        userRepository.save(u);

        return "redirect:/login";
    }
}
