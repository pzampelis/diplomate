package com.petros.diplomate.service;

import com.petros.diplomate.model.SecureToken;
import com.petros.diplomate.model.User;
import com.petros.diplomate.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecureTokenService secureTokenService;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @MockBean
    private EmailService emailService;

    @InjectMocks
    private UserService userService;


    @Test
    void givingANewEmail_whenSigningUpANewUser_shouldCallSave() {
        User user = new User("aFirstName", "aLastName", "anemail@mail.com", "aPassword", "STUDENT_ROLE");

        userService.signUpUser(user);

        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void givingAnEmailThatHasBeenAlreadyGivenAndVerified_whenSigningUpANewUser_shouldThrowException() {
        User user1 = new User("fn1", "ln1", "e1", "p1", "STUDENT_ROLE");
        user1.setEnabled(true);
        user1.setId(1L);
        User user2 = new User("fn2", "ln2", "e1", "p2", "STUDENT_ROLE");

        Mockito.when(userRepository.findByEmail("e1")).thenReturn(java.util.Optional.of(user1));
        SecureToken secureToken = new SecureToken();
        secureToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        Mockito.when(secureTokenService.getSecureTokenByUserId(1L)).thenReturn(secureToken);
        IllegalStateException exception = null;
        try {
            userService.signUpUser(user2);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("Email already taken", exception.getMessage());
    }

    @Test
    void givingAnEmailThatHasBeenAlreadyGivenButNotVerifiedYet_whenSigningUpANewUser_shouldThrowException() {
        User user1 = new User("fn1", "ln1", "e1", "p1", "STUDENT_ROLE");
        user1.setId(1L);
        User user2 = new User("fn2", "ln2", "e1", "p2", "STUDENT_ROLE");

        Mockito.when(userRepository.findByEmail("e1")).thenReturn(java.util.Optional.of(user1));
        SecureToken secureToken = new SecureToken();
        secureToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        Mockito.when(secureTokenService.getSecureTokenByUserId(1L)).thenReturn(secureToken);
        IllegalStateException exception = null;
        try {
            userService.signUpUser(user2);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("Email has already been given but has not been verified yet", exception.getMessage());
    }

    @Test
    void givingAnEmailThatHasBeenAlreadyGivenButExpirationHasPassed_whenSigningUpANewUser_shouldNotThrowException() {
        User user1 = new User("fn1", "ln1", "e1", "p1", "STUDENT_ROLE");
        user1.setId(1L);
        User user2 = new User("fn2", "ln2", "e1", "p2", "STUDENT_ROLE");

        Mockito.when(userRepository.findByEmail("e1")).thenReturn(java.util.Optional.of(user1));
        SecureToken secureToken = new SecureToken();
        secureToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        Mockito.when(secureTokenService.getSecureTokenByUserId(1L)).thenReturn(secureToken);
        IllegalStateException exception = null;
        try {
            userService.signUpUser(user2);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertNull(exception);
    }

    @Test
    void givingAValidVerifiedEmail_whenAskingToResetPassword_shouldUseEmailSendOneTime() {
        User user = new User("aFirstName", "aLastName", "anemail@mail.com", "aPassword", "STUDENT_ROLE");
        user.setEnabled(true);

        Mockito.when(userRepository.findByEmail("anemail@mail.com")).thenReturn(java.util.Optional.of(user));
        userService.resetPassword("anemail@mail.com");

        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void givingAValidNotVerifiedEmail_whenAskingToResetPassword_shouldThrowException() {
        User user = new User("aFirstName", "aLastName", "anemail@mail.com", "aPassword", "STUDENT_ROLE");

        Mockito.when(userRepository.findByEmail("anemail@mail.com")).thenReturn(java.util.Optional.of(user));
        IllegalStateException exception = null;
        try {
            userService.resetPassword("anemail@mail.com");
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("Account is not activated", exception.getMessage());
    }

    @Test
    void givingAnInvalidEmail_whenAskingToResetPassword_shouldThrowException() {
        IllegalStateException exception = null;
        try {
            userService.resetPassword("anemail@mail.com");
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("User with email anemail@mail.com not found", exception.getMessage());
    }

}
