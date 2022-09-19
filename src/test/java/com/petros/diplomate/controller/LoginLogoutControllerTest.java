package com.petros.diplomate.controller;

import com.petros.diplomate.model.Professor;
import com.petros.diplomate.model.User;
import com.petros.diplomate.service.SessionService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class LoginLogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SessionService sessionService;

    @Mock
    private Authentication auth;


    @BeforeEach
    public void initSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void redirectToHomePage_whenUserRoleIsAdmin_shouldRedirectToAdminHomePage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "ADMIN_ROLE");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(get("/api/redirect-home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/admin/home"));
    }

    @Test
    void redirectToHomePage_whenUserRoleIsProfessor_shouldRedirectToProfessorProfilePage() throws Exception {
        User user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(get("/api/redirect-home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/"  + user.getId() + "/profile"));
    }

    @Test
    void redirectToHomePage_whenUserRoleIsStudent_shouldRedirectToStudentProfilePage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        user.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(get("/api/redirect-home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/student/"  + user.getId() + "/profile"));
    }

}
