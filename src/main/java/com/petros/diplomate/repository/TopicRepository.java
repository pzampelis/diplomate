package com.petros.diplomate.repository;

import com.petros.diplomate.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByStudentId(Long studentId);

    Optional<Topic> findByName(String name);

    List<Topic> findAllByProfessorId(Long professorId);

}
