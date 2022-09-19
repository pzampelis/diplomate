package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Topic {

    @Id
    @SequenceGenerator(name = "topic_sequence", sequenceName = "topic_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "topic_sequence")
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(columnDefinition="text")
    private String description;

    private boolean isActive = true;
    private String status = "Applications period";      // Applications period, Interviews period
    private Double score;
    private String type = "";                           // STANDARD_TYPE, OPEN_TYPE

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime acceptanceDeadline = null;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicationDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "professor_id")
    private Professor professor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_topic_interest", joinColumns = @JoinColumn(name = "topic_id"),
               inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<Student> candidates = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_open_topic_rejection", joinColumns = @JoinColumn(name = "topic_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<Student> notInterestedStudents = new ArrayList<>();

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequiredCourse> requiredCourses = new ArrayList<>();

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SelectedTopicTag> selectedTopicTags = new ArrayList<>();


    public Topic(String name, String description, LocalDate applicationDeadline, Professor professor, String type) {
        this.name = name;
        this.description = description;
        this.applicationDeadline = applicationDeadline;
        this.professor = professor;
        this.type = type;
    }

}
