package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.model.Role;
import hu.tamas.szakdolgozatjava.model.User;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import hu.tamas.szakdolgozatjava.service.AdminUserService;
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
    private final AdminUserService adminUserService;

    public AdminUsersController(UserRepository userRepository, AdminUserService adminUserService) {
        this.userRepository = userRepository;
        this.adminUserService = adminUserService;
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
        model.addAttribute("allRoles", EnumSet.allOf(Role.class));
        return "admin/user-edit";
    }

    @PostMapping("/{id}/edit")
    public String editUserSubmit(
            @PathVariable Long id,
            @ModelAttribute("user") User formUser,
            @RequestParam(name = "roles", required = false) Set<Role> roles
    ) {
        User user = userRepository.findById(id).orElseThrow();

        user.setEmail(formUser.getEmail());
        user.setFirstName(formUser.getFirstName());
        user.setLastName(formUser.getLastName());
        user.setBirthDate(formUser.getBirthDate());
        user.setHobby(formUser.getHobby());

        if (roles != null && !roles.isEmpty()) {
            user.setRoles(new HashSet<>(roles));
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, Principal principal) {
        User target = userRepository.findById(id).orElseThrow();

        if (principal != null && principal.getName().equals(target.getUsername())) {
            return "redirect:/admin/users?selfDeleteError";
        }

        adminUserService.deleteUserHard(id);
        return "redirect:/admin/users?deleted";
    }
}
