package com.petros.diplomate.security;

import com.petros.diplomate.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class Guard {

    @Autowired
    private UserDetailsService userService;

    public boolean checkUserId(Authentication authentication, int id) {
        String email = authentication.getName();
        User foundUser = (User) userService.loadUserByUsername(email);

        return foundUser.getId() == id;
    }

}
