package com.petros.diplomate.controller;

import com.petros.diplomate.model.User;
import com.petros.diplomate.service.CourseService;
import com.petros.diplomate.service.TagService;
import com.petros.diplomate.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private TagService tagService;

    @Mock
    private Authentication auth;


    @BeforeEach
    public void initSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void providingValidParams_whenUpdatingAdminPassword_shouldRedirectToAdminHome() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "ADMIN_ROLE");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(post("/api/admin/update-password")
                        .param("oldPassword", "password")
                        .param("newPassword", "newPassword")
                        .param("reenteredNewPassword", "newPassword")
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/admin/home"));
    }

    @Test
    void providingInvalidParams_whenUpdatingAdminPassword_shouldRedirectToUpdatePasswordPage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "ADMIN_ROLE");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(userService).changePassword(any(User.class), anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/admin/update-password")
                        .param("oldPassword", "password")
                        .param("newPassword", "newPassword")
                        .param("reenteredNewPassword", "anotherNewPassword")
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/admin/update-password"));
    }

    @Test
    void providingValidList_whenUpdatingCoursesList_shouldRedirectToAdminHome() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "ADMIN_ROLE");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(post("/api/admin/update-courses")
                        .flashAttr("courses", "course1\n\rcourse1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/admin/home"));
    }

    @Test
    void providingInvalidList_whenUpdatingCoursesList_shouldReturnUpdateCoursesPage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "ADMIN_ROLE");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(courseService).updateCoursesList(anyString());

        mockMvc.perform(post("/api/admin/update-courses")
                        .flashAttr("courses", "course1\n\rcourse2"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-courses.html"));
    }

    @Test
    void providingValidList_whenUpdatingTagsList_shouldRedirectToAdminHome() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "ADMIN_ROLE");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(post("/api/admin/update-tags")
                        .flashAttr("courses", "tag1\n\rtag2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/admin/home"));
    }

    @Test
    void providingInvalidList_whenUpdatingTagsList_shouldReturnUpdateTagsPage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "ADMIN_ROLE");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(tagService).updateTagsList(anyString());

        mockMvc.perform(post("/api/admin/update-tags")
                        .flashAttr("courses", "tag1\n\rtag1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-tags.html"));
    }

}
