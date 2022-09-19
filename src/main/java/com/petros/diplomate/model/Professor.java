package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Professor extends User {

    private Integer phoneNumber = null;
    private String officeInfo = "";
    private String webpageLink = "";


    public Professor(String firstName, String lastName, String email, String password, String userRole) {
        super(firstName, lastName, email, password, userRole);
    }

}
