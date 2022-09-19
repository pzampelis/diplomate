package com.petros.diplomate.controller;

import com.petros.diplomate.model.Professor;
import com.petros.diplomate.model.User;
import com.petros.diplomate.service.SessionService;
import com.petros.diplomate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class LoginLogoutController {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;


    @GetMapping("/login")
    public String showLoginForm() {
        // Logout user if he/she goes back to the login page by using the back button
        SecurityContextHolder.clearContext();

        return "login.html";
    }

    @GetMapping("/logout")
    public String processLogoutRequest() {
        SecurityContextHolder.clearContext();

        return "redirect:/api/login";
    }

    @GetMapping("/redirect-home")
    public String redirectToHomePage() {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());

        if (currentUser.getRole().equals("STUDENT_ROLE"))
            return "redirect:/api/student/"  + currentUser.getId() + "/profile";
        else if (currentUser.getRole().equals("PROFESSOR_ROLE")) {
            sessionService.checkForUnratedCompletedExaminations((Professor) currentUser);
            return "redirect:/api/professor/" + currentUser.getId() + "/profile";
        }

        return "redirect:/api/admin/home";
    }

}
