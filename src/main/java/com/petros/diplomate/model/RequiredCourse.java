package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class RequiredCourse {

    @Id
    @SequenceGenerator(name = "required_course_sequence", sequenceName = "required_course_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "required_course_sequence")
    private Long id;

    @OneToOne
    @JoinColumn(nullable = false, name = "course_id")
    private Course course;
    private double minScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "topic_id")
    private Topic topic;


    public RequiredCourse(Course course, double minScore, Topic topic) {
        this.course = course;
        this.minScore = minScore;
        this.topic = topic;
    }

}
