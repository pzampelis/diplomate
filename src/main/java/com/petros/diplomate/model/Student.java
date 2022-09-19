package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Student extends User {

    private String gradesPDFPath = "";

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SelectedStudentTag> selectedStudentTags = new ArrayList<>();


    public Student(String firstName, String lastName, String email, String password, String userRole) {
        super(firstName, lastName, email, password, userRole);
    }

}
