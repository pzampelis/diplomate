package com.petros.diplomate.repository;

import com.petros.diplomate.model.ExaminationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExaminationScoreRepository extends JpaRepository<ExaminationScore, Long> {

    Optional<ExaminationScore> findByProfessorIdAndTopicId(Long professorId, Long topicId);

    List<ExaminationScore> findAllByTopicId(Long topicId);

}
