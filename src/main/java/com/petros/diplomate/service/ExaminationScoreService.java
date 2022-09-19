package com.petros.diplomate.service;

import com.petros.diplomate.model.ExaminationScore;
import com.petros.diplomate.model.Professor;
import com.petros.diplomate.model.Topic;
import com.petros.diplomate.repository.ExaminationScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExaminationScoreService {

    @Autowired
    private ExaminationScoreRepository examinationScoreRepository;

    @Autowired
    private TopicService topicService;

    @Autowired
    private EmailService emailService;


    public Optional<ExaminationScore> getExaminationScoreByProfessorIdAndTopicId(Long professorId, Long topicId) {
        return examinationScoreRepository.findByProfessorIdAndTopicId(professorId, topicId);
    }

    public void rateExamination(Professor professor, Topic topic, double presentationScore, double textScore,
                                double methodologyScore, Double preparationScore) {

        if (preparationScore == null)
            preparationScore = -1.0;

        ExaminationScore newExaminationScore = new ExaminationScore(professor, topic, presentationScore, textScore,
                                                                    methodologyScore, preparationScore);

        examinationScoreRepository.save(newExaminationScore);
    }

    public void completeExaminationScoring(Topic topic) {
        List<ExaminationScore> topicScores = examinationScoreRepository.findAllByTopicId(topic.getId());

        if (topicScores.size() == 3) {
            double averagePresentationScore = 0.0;
            double averageTextScore = 0.0;
            double averageMethodologyScore = 0.0;
            double averagePreparationScore = 0.0;
            double averageTotalScore = 0.0;

            for (ExaminationScore examScore : topicScores) {
                averagePresentationScore += examScore.getPresentationScore();
                averageTextScore += examScore.getTextScore();
                averageMethodologyScore += examScore.getMethodologyScore();

                if (examScore.getPreparationScore() != -1.0)
                    averagePreparationScore = examScore.getPreparationScore();
            }

            averagePresentationScore = averagePresentationScore / 3;
            averageTextScore = averageTextScore / 3;
            averageMethodologyScore = averageMethodologyScore / 3;
            averageTotalScore = (0.1 * averagePresentationScore) + (0.2 * averageTextScore) + (0.4 * averageMethodologyScore) +
                                (0.3 * averagePreparationScore);

            double formattedAverageTotalScore = Math.round(averageTotalScore * 2) / 2.0;

            topic.setScore(formattedAverageTotalScore);
            topic.setActive(false);
            topicService.saveTopic(topic);

            String subject = "Topic Scored";
            emailService.send(topic.getProfessor().getEmail(),
                    emailService.buildTopicScoredEmail(topic.getProfessor().getFirstName(), topic.getName()), subject);
            emailService.send(topic.getStudent().getEmail(),
                    emailService.buildTopicScoredEmail(topic.getStudent().getFirstName(), topic.getName()), subject);
        }
    }

}
