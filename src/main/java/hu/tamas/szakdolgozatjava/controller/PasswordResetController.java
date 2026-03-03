package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.dto.ForgotPasswordForm;
import hu.tamas.szakdolgozatjava.dto.ResetPasswordForm;
import hu.tamas.szakdolgozatjava.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("form", new ForgotPasswordForm());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordPost(@ModelAttribute("form") ForgotPasswordForm form,
                                     HttpServletRequest request,
                                     RedirectAttributes ra) {

        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());

        passwordResetService.requestReset(form.getEmail(), baseUrl);

        ra.addFlashAttribute("success",
                "Ha a megadott e-mail cím létezik, elküldtük a jelszó helyreállító linket.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                Model model,
                                RedirectAttributes ra) {
        try {
            passwordResetService.validateToken(token);
            ResetPasswordForm f = new ResetPasswordForm();
            f.setToken(token);
            model.addAttribute("form", f);
            return "auth/reset-password";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/reset-password")
    public String resetPasswordPost(@ModelAttribute("form") ResetPasswordForm form,
                                    RedirectAttributes ra) {

        if (form.getPassword() == null || form.getPassword().length() < 6) {
            ra.addFlashAttribute("error", "A jelszó legalább 6 karakter legyen.");
            return "redirect:/reset-password?token=" + form.getToken();
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            ra.addFlashAttribute("error", "A két jelszó nem egyezik.");
            return "redirect:/reset-password?token=" + form.getToken();
        }

        try {
            passwordResetService.resetPassword(form.getToken(), form.getPassword());
            ra.addFlashAttribute("success", "Jelszó sikeresen megváltoztatva. Jelentkezz be!");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + form.getToken();
        }
    }
}