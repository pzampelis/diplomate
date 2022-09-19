package com.petros.diplomate.controller;

import com.petros.diplomate.model.User;
import com.petros.diplomate.service.UploadedFileService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UploadedFileService uploadedFileService;

    @Mock
    private Authentication auth;


    @BeforeEach
    public void initSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void providingValidParams_whenUpdatingStudentPassword_shouldRedirectToStudentProfilePage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        user.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(post("/api/student/{id}/profile/update-password", user.getId())
                        .param("oldPassword", "password")
                        .param("newPassword", "newPassword")
                        .param("reenteredNewPassword", "newPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/student/" + user.getId() + "/profile"));
    }

    @Test
    void providingInvalidParams_whenUpdatingStudentPassword_shouldRedirectToUpdatePasswordPage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        user.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(userService).changePassword(any(User.class), anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/student/{id}/profile/update-password", user.getId())
                        .param("oldPassword", "password")
                        .param("newPassword", "newPassword")
                        .param("reenteredNewPassword", "anotherNewPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/student/" + user.getId() + "/profile/update-password"));
    }

    @Test
    void providingValidParams_whenUpdatingProfessorPassword_shouldRedirectToProfessorProfilePage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(post("/api/professor/{id}/profile/update-password", user.getId())
                        .param("oldPassword", "password")
                        .param("newPassword", "newPassword")
                        .param("reenteredNewPassword", "newPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/profile"));
    }

    @Test
    void providingInvalidParams_whenUpdatingProfessorPassword_shouldRedirectToUpdatePasswordPage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(userService).changePassword(any(User.class), anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/professor/{id}/profile/update-password", user.getId())
                        .param("oldPassword", "password")
                        .param("newPassword", "newPassword")
                        .param("reenteredNewPassword", "anotherNewPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/profile/update-password"));
    }

}
