package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class SelectedStudentTag extends SelectedTag {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "student_id")
    private Student student;


    public SelectedStudentTag(boolean isSelected, Tag tag, Student student) {
        super(isSelected, tag, "STUDENT_TYPE");
        this.student = student;
    }

}
