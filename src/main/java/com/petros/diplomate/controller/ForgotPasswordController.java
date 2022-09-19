package com.petros.diplomate.controller;

import com.petros.diplomate.model.SecureToken;
import com.petros.diplomate.model.User;
import com.petros.diplomate.service.SecureTokenService;
import com.petros.diplomate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api")
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecureTokenService secureTokenService;


    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password.html";
    }

    @PostMapping("/forgot-password")
    public String processForgotPasswordForm(@RequestParam("email") String email,  RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(email);
            redirectAttributes.addFlashAttribute("message", "A reset password link has been sent" +
                                                 " to your email, please check your inbox");

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/api/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@Param("token") String token, Model model) {
        SecureToken secureToken = secureTokenService.getSecureTokenByToken(token);
        User user = secureToken.getUser();

        if (user == null)
            model.addAttribute("message", "Invalid token");

        model.addAttribute("token", token);
        userService.confirmSecureToken(token);

        return "reset-password.html";
    }

    @PostMapping("/reset-password")
    public String processResetPasswordForm(@RequestParam("token") String token, @RequestParam("password") String password,
                                           @RequestParam("reenteredPassword") String reenteredPassword,
                                           RedirectAttributes redirectAttributes) {

        SecureToken secureToken = secureTokenService.getSecureTokenByToken(token);

        if (secureToken == null)
            redirectAttributes.addFlashAttribute("message", "Invalid token");
        else
            try {
                userService.updatePassword(secureToken.getUser().getUsername(), password, reenteredPassword);
                secureTokenService.emptyToken(secureToken);
                redirectAttributes.addFlashAttribute("message", "Your password was updated successfully");
            } catch (IllegalStateException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                redirectAttributes.addAttribute("token", token);

                return "redirect:/api/reset-password";
            }

        return "redirect:/api/login";
    }

}
