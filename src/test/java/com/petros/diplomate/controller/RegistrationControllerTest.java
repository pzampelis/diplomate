package com.petros.diplomate.controller;

import com.petros.diplomate.model.RegistrationRequest;
import com.petros.diplomate.model.User;
import com.petros.diplomate.service.RegistrationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private Authentication auth;

    @MockBean
    private RegistrationService registrationService;


    @BeforeEach
    public void initSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void goingToRegistrationPageLink_whenWantingToRegisterForFirstTime_shouldReturnRegisterPage() throws Exception {
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("anonymousUser");

        mockMvc.perform(get("/api/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration.html"));
    }

    @Test
    void goingToRegistrationPageLinkUsingTheBackButton_whenAlreadyMadeRegistrationRequest_shouldRedirectToLoginPage()
            throws Exception {

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("notAnonymousUser");

        mockMvc.perform(get("/api/register"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/login"));
    }

    @Test
    void registeringUser_whenUserIsValidAndDoesNotAlreadyExists_shouldRedirectToLoginPage() throws Exception {
        mockMvc.perform(post("/api/register")
                        .param("reenteredPassword", "aPassword")
                        .flashAttr("user", new User()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/login"));
    }

    @Test
    void registeringUser_whenUserIsNotValidOrAlreadyExists_shouldReturnRegistrationPage() throws Exception {
        Mockito.doThrow(new IllegalStateException()).when(registrationService).registerNewUser(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/register")
                        .param("reenteredPassword", "aPassword")
                        .flashAttr("user", new User()))
                .andExpect(status().isOk())
                .andExpect(view().name("registration.html"));
    }

}
