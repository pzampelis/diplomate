package com.petros.diplomate.service;

import com.petros.diplomate.model.Professor;
import com.petros.diplomate.model.Student;
import com.petros.diplomate.model.User;
import com.petros.diplomate.model.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;


    public void registerNewUser(RegistrationRequest request) throws IllegalStateException {
        emailService.checkEmailValidity(request.getEmail());

        if (!request.getPassword().equals(request.getReenteredPassword()))
            throw new IllegalStateException("Passwords do not match");

        User newUser = null;

        if (request.getUserRole().equals("STUDENT_ROLE"))
            newUser = new Student(request.getFirstName(), request.getLastName(), request.getEmail(),
                                  request.getPassword(), request.getUserRole());
        else
            newUser = new Professor(request.getFirstName(), request.getLastName(), request.getEmail(),
                                    request.getPassword(), request.getUserRole());

        String token = userService.signUpUser(newUser);

        String link = "http://localhost:8080/api/registration/confirm?token=" + token;
        String subject = "Verify Your Email";
        emailService.send(request.getEmail(), emailService.buildLinkEmail(request.getFirstName(), subject, link), subject);
    }

}
