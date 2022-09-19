package com.petros.diplomate.service;

import com.petros.diplomate.model.RegistrationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserService userService;

    @InjectMocks
    private RegistrationService registrationService;


    @Test
    void givingSamePasswords_whenRegisteringNewUser_shouldCallEmailSendOneTime() {
        RegistrationRequest request = new RegistrationRequest("fn", "ln", "e",
                                                              "pw", "pw", "STUDENT_ROLE");

        registrationService.registerNewUser(request);

        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void givingDifferentPasswords_whenRegisteringNewUser_shouldThrowException() {
        RegistrationRequest request = new RegistrationRequest("fn", "ln", "e",
                                                              "pw1", "pw2", "STUDENT_ROLE");

        IllegalStateException exception = null;
        try {
            registrationService.registerNewUser(request);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("Passwords do not match", exception.getMessage());
    }

}
