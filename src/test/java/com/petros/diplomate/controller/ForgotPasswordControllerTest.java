package com.petros.diplomate.controller;

import com.petros.diplomate.model.SecureToken;
import com.petros.diplomate.model.User;
import com.petros.diplomate.service.SecureTokenService;
import com.petros.diplomate.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ForgotPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SecureTokenService secureTokenService;


    @Test
    void accessingForgotPasswordPage_shouldDisplayForgotPasswordPageAtGetAndRedirectToItAtPost() throws Exception {
        mockMvc.perform(get("/api/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password.html"));

        mockMvc.perform(post("/api/forgot-password")
                        .param("email", "student@mail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/forgot-password"));
    }

    @Test
    void providingInvalidToken_whenResettingPassword_shouldRedirectToLoginPage() throws Exception {
        Mockito.when(secureTokenService.getSecureTokenByToken("aToken")).thenReturn(null);

        mockMvc.perform(post("/api/reset-password")
                        .param("token", "aToken")
                        .param("password", "newPassword")
                        .param("reenteredPassword", "newPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/login"));
    }

    @Test
    void providingValidToken_whenResettingPassword_shouldRedirectToLoginPage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        SecureToken secureToken = new SecureToken("aToken", LocalDateTime.now().plusMinutes(1), false, user);

        Mockito.when(secureTokenService.getSecureTokenByToken("aToken")).thenReturn(secureToken);

        mockMvc.perform(post("/api/reset-password")
                        .param("token", "aToken")
                        .param("password", "newPassword")
                        .param("reenteredPassword", "newPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/login"));
    }

    @Test
    void providingValidTokenAndWrongPasswords_whenResettingPassword_shouldRedirectToResetPasswordPage() throws Exception {
        User user = new User("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        SecureToken secureToken = new SecureToken("aToken", LocalDateTime.now().plusMinutes(1), false, user);

        Mockito.when(secureTokenService.getSecureTokenByToken("aToken")).thenReturn(secureToken);
        Mockito.doThrow(new IllegalStateException()).when(userService).updatePassword(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/reset-password")
                        .param("token", "aToken")
                        .param("password", "newPassword")
                        .param("reenteredPassword", "anotherNewPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/reset-password"));
    }

}
