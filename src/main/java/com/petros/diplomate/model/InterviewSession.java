package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class InterviewSession extends Session {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "topic_id")
    private Topic topic;

    private Double score;


    public InterviewSession(String type, LocalDate date, LocalTime time, Professor professor, Student student, Topic topic) {
        super(type, date, time, professor);
        this.student = student;
        this.topic = topic;
    }

}
