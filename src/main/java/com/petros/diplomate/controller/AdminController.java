package com.petros.diplomate.controller;

import com.petros.diplomate.model.User;
import com.petros.diplomate.service.CourseService;
import com.petros.diplomate.service.TagService;
import com.petros.diplomate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TagService tagService;


    @GetMapping("/home")
    public String showAdminHomePage() {
        return "admin-home.html";
    }

    @GetMapping("/update-password")
    public String showUpdatePasswordForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        return "update-password.html";
    }

    @PostMapping("/update-password")
    public String processUpdatePasswordForm(@RequestParam("oldPassword") String oldPassword,
                                            @RequestParam("newPassword") String newPassword,
                                            @RequestParam("reenteredNewPassword") String reenteredNewPassword,
                                            RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());

        try {
            userService.changePassword(currentUser, oldPassword, newPassword, reenteredNewPassword);
            redirectAttributes.addFlashAttribute("message", "Password updated successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/api/admin/update-password";
        }

        return "redirect:/api/admin/home";
    }

    @GetMapping("/update-courses")
    public String showCourseHandlingForm(Model model) {
        String courses = courseService.getCoursesAsString();
        model.addAttribute("courses", courses);

        return "update-courses.html";
    }

    @PostMapping("/update-courses")
    public String processUpdateCoursesForm(@ModelAttribute("courses") String updatedCourses, Model model,
                                           RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCoursesList(updatedCourses);
            redirectAttributes.addFlashAttribute("message", "List successfully updated");
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("courses", updatedCourses);

            return "update-courses.html";
        }

        return "redirect:/api/admin/home";
    }

    @GetMapping("/update-tags")
    public String showTagHandlingForm(Model model) {
        String tags = tagService.getTagsAsString();
        model.addAttribute("tags", tags);

        return "update-tags.html";
    }

    @PostMapping("/update-tags")
    public String processUpdateTagsForm(@ModelAttribute("tags") String updatedTags, Model model,
                                           RedirectAttributes redirectAttributes) {
        try {
            tagService.updateTagsList(updatedTags);
            redirectAttributes.addFlashAttribute("message", "List successfully updated");
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tags", updatedTags);

            return "update-tags.html";
        }

        return "redirect:/api/admin/home";
    }

}
