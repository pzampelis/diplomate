package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class ExaminationScore {

    @Id
    @SequenceGenerator(name = "examination_score_sequence", sequenceName = "examination_score_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "examination_score_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "professor_id")
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "topic_id")
    private Topic topic;

    private double presentationScore;
    private double textScore;
    private double methodologyScore;
    private double preparationScore;


    public ExaminationScore(Professor professor, Topic topic, double presentationScore, double textScore, double methodologyScore,
                            double preparationScore) {

        this.professor = professor;
        this.topic = topic;
        this.presentationScore = presentationScore;
        this.textScore = textScore;
        this.methodologyScore = methodologyScore;
        this.preparationScore = preparationScore;
    }

}
