package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class ExaminationSession extends Session {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", referencedColumnName = "id")
    private Topic topic;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "professor_examination_session", joinColumns = @JoinColumn(name = "examination_session_id"),
               inverseJoinColumns = @JoinColumn(name = "professor_id"))
    private List<Professor> invitedProfessors = new ArrayList<>();


    public ExaminationSession(String type, LocalDate date, LocalTime time, Professor professor, Student student, Topic topic) {
        super(type, date, time, professor);
        this.student = student;
        this.topic = topic;
    }

}
