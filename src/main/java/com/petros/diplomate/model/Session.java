package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Session {

    @Id
    @SequenceGenerator(name = "session_sequence", sequenceName = "session_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_sequence")
    private Long id;

    private String type;               // EXAMINATION_TYPE, INTERVIEW_TYPE, MEETING_TYPE

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "professor_id")
    private Professor professor;


    public Session(String type, LocalDate date, LocalTime time, Professor professor) {
        this.type = type;
        this.date = date;
        this.time = time;
        this.professor = professor;
    }

}
