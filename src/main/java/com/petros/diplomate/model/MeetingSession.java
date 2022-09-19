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
public class MeetingSession extends Session {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_meeting_session", joinColumns = @JoinColumn(name = "meeting_session_id"),
               inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<Student> invitedStudents = new ArrayList<>();


    public MeetingSession(String type, LocalDate date, LocalTime time, Professor professor) {
        super(type, date, time, professor);
    }

}
