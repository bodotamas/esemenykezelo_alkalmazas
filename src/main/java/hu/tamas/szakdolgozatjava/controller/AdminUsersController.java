package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.model.Role;
import hu.tamas.szakdolgozatjava.model.User;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {

    private final UserRepository userRepository;

    public AdminUsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();

        model.addAttribute("user", user);
        model.addAttribute("allRoles", EnumSet.allOf(Role.class)); // LAKOS, SZERVEZO, ADMIN
        return "admin/user-edit";
    }

    @PostMapping("/{id}/edit")
    public String editUserSubmit(
            @PathVariable Long id,
            @ModelAttribute("user") User formUser,
            @RequestParam(name = "roles", required = false) Set<Role> roles
    ) {
        User user = userRepository.findById(id).orElseThrow();

        // alap profil mezők frissítése
        user.setEmail(formUser.getEmail());
        user.setFirstName(formUser.getFirstName());
        user.setLastName(formUser.getLastName());
        user.setBirthDate(formUser.getBirthDate());
        user.setHobby(formUser.getHobby());

        // role-ok frissítése (ha semmit nem jelöl: üres lenne -> ezt nem engedjük)
        if (roles == null || roles.isEmpty()) {
            // ha nincs kiválasztva semmi, hagyjuk meg a korábbit
            // (így nem tudod véletlenül “role nélkül” menteni)
        } else {
            user.setRoles(new HashSet<>(roles));
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, Principal principal) {
        User target = userRepository.findById(id).orElseThrow();

        // ne tudd saját magad törölni (erősen ajánlott)
        if (principal != null && principal.getName().equals(target.getUsername())) {
            return "redirect:/admin/users?selfDeleteError";
        }

        userRepository.deleteById(id);
        return "redirect:/admin/users?deleted";
    }
}