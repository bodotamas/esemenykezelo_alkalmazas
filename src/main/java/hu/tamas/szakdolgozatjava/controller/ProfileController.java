package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.repository.UserRepository;
import hu.tamas.szakdolgozatjava.service.ProfileImageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final ProfileImageService profileImageService;

    public ProfileController(UserRepository userRepository, ProfileImageService profileImageService) {
        this.userRepository = userRepository;
        this.profileImageService = profileImageService;
    }

    @GetMapping("/profile")
    public String profileView(Model model, Principal principal) {
        var user = userRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/profile/edit")
    public String editForm(Model model, Principal principal) {
        var user = userRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/profile/edit")
    public String editSubmit(
            @ModelAttribute("user") hu.tamas.szakdolgozatjava.model.User formUser,
            @RequestParam(name = "profileImage", required = false) MultipartFile profileImage,
            Principal principal
    ) {
        var user = userRepository.findByUsername(principal.getName()).orElseThrow();

        user.setEmail(formUser.getEmail());
        user.setFirstName(formUser.getFirstName());
        user.setLastName(formUser.getLastName());
        user.setBirthDate(formUser.getBirthDate());
        user.setHobby(formUser.getHobby());

        // PROFILKÉP: ha töltött fel újat, töröljük a régit és mentjük az újat
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                profileImageService.deleteIfExists(user.getProfileImagePath());
                String newPath = profileImageService.saveProfileImage(profileImage);
                user.setProfileImagePath(newPath);
            }
        } catch (IllegalArgumentException ex) {
            // egyszerű megoldás: visszadobjuk ugyanarra az oldalra egy query parammal
            // (Ha akarsz "szép" field error-t, akkor csinálunk BindingResult-os verziót is.)
            return "redirect:/profile/edit?imgError";
        } catch (Exception ex) {
            return "redirect:/profile/edit?imgError";
        }

        userRepository.save(user);
        return "redirect:/profile";
    }
}
