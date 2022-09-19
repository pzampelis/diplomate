package com.petros.diplomate.controller;

import com.petros.diplomate.model.SecureToken;
import com.petros.diplomate.model.User;
import com.petros.diplomate.model.RegistrationRequest;
import com.petros.diplomate.service.RegistrationService;
import com.petros.diplomate.service.SecureTokenService;
import com.petros.diplomate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecureTokenService secureTokenService;


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal != "anonymousUser") {
            SecurityContextHolder.clearContext();
            return "redirect:/api/login";
        }

        model.addAttribute("user", new User());

        return "registration.html";
    }

    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes,
                                          @RequestParam("reenteredPassword") String reenteredPassword, Model model) {

        RegistrationRequest request = new RegistrationRequest(user.getFirstName(), user.getLastName(), user.getEmail(),
                                                              user.getPassword(), reenteredPassword, user.getRole());

        try {
            registrationService.registerNewUser(request);
            redirectAttributes.addFlashAttribute("message",
                                                 "A verification link has been sent to your email");
        } catch (IllegalStateException e) {
            model.addAttribute("user", user);
            model.addAttribute("error", e.getMessage());
            return "registration.html";
        }

        return "redirect:/api/login";
    }

    @GetMapping("/registration/confirm")
    public String confirmRegistration(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        userService.confirmSecureToken(token);
        SecureToken secureToken = secureTokenService.getSecureTokenByToken(token);
        secureTokenService.emptyToken(secureToken);

        redirectAttributes.addFlashAttribute("message", "Your registration was successfully completed");

        return "redirect:/api/login";
    }

}
