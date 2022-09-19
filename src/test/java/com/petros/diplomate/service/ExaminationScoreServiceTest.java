package com.petros.diplomate.service;

import com.petros.diplomate.model.*;
import com.petros.diplomate.repository.ExaminationScoreRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class ExaminationScoreServiceTest {

    @MockBean
    private TopicService topicService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private ExaminationScoreRepository examinationScoreRepository;

    @InjectMocks
    private ExaminationScoreService examinationScoreService;


    @Test
    void scoringAnExamination_whenUsingRateExamination_shouldCallSaveOneTime() {
        examinationScoreService.rateExamination(new Professor(), new Topic(), 10.0,
                                                10.0, 10.0, 10.0);

        Mockito.verify(examinationScoreRepository, Mockito.times(1)).save(Mockito.any(ExaminationScore.class));
    }

    @Test
    void whenExaminationScoringIsComplete_shouldCallEmailSendTwoTimesAndSetTopicScoreToTheRightScore() {
        Professor professor1 = new Professor("pfn1", "pln1", "pe1", "pp1", "PROFESSOR_ROLE");
        Professor professor2 = new Professor("pfn2", "pln2", "pe2", "pp2", "PROFESSOR_ROLE");
        Professor professor3 = new Professor("pfn3", "pln3", "pe3", "pp3", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor1, "STANDARD_TYPE");
        topic.setId(1L);
        topic.setStudent(student);
        ExaminationScore score1 = new ExaminationScore(professor1, topic, 10, 9, 8, 7);
        ExaminationScore score2 = new ExaminationScore(professor2, topic, 9, 8, 7, -1);
        ExaminationScore score3 = new ExaminationScore(professor3, topic, 10, 9, 8, -1);
        List<ExaminationScore> allScore = new ArrayList<>();
        allScore.add(score1);
        allScore.add(score2);
        allScore.add(score3);

        Mockito.when(examinationScoreRepository.findAllByTopicId(1L)).thenReturn(allScore);
        examinationScoreService.completeExaminationScoring(topic);

        Mockito.verify(emailService, Mockito.times(2)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
        Assertions.assertEquals(8.0, topic.getScore());
    }

}
