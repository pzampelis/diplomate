package com.petros.diplomate.service;

import com.petros.diplomate.model.Professor;
import com.petros.diplomate.model.Student;
import com.petros.diplomate.model.Topic;
import com.petros.diplomate.repository.TopicRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @MockBean
    private TopicRepository topicRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private SelectedTagService selectedTagService;

    @MockBean
    private TagService tagService;

    @InjectMocks
    private TopicService topicService;


    @Test
    void creatingAStandardTopic_whenCreatingATopic_shouldReturnAStandardTopic() {
        Professor professor = new Professor("fn", "ln", "e", "p", "PROFESSOR_ROLE");
        Topic topic = new Topic("aName", "aDescription", null, null, null);

        Topic createdTopic = topicService.createTopic(topic, "STANDARD_TYPE", professor, new ArrayList<>());

        Assertions.assertEquals(topic.getName(), createdTopic.getName());
        Assertions.assertEquals("STANDARD_TYPE", createdTopic.getType());
    }

    @Test
    void creatingAnOpenTopic_whenCreatingATopic_shouldReturnAnOpenTopic() {
        Professor professor = new Professor("fn", "ln", "e", "p", "PROFESSOR_ROLE");
        Topic topic = new Topic("aName", "aDescription", null, null, null);

        Topic createdTopic = topicService.createTopic(topic, "OPEN_TYPE", professor, new ArrayList<>());

        Assertions.assertEquals(topic.getName(), createdTopic.getName());
        Assertions.assertEquals("OPEN_TYPE", createdTopic.getType());
    }

    @Test
    void givingANameThatAlreadyExists_whenCreatingATopic_shouldThrowException() {
        Professor professor = new Professor("fn", "ln", "e", "p", "PROFESSOR_ROLE");
        Topic topic1 = new Topic("aName", "d1", LocalDate.now().plusDays(1), professor, "STANDARD_TYPE");
        Topic topic2 = new Topic("aName", "d2", null, null, null);

        Mockito.when(topicRepository.findByName("aName")).thenReturn(java.util.Optional.of(topic1));
        IllegalStateException exception = null;
        try {
            topicService.createTopic(topic2, "OPEN_TYPE", professor, new ArrayList<>());
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("This name is already taken", exception.getMessage());
    }

    @Test
    void updatingATopicDescription_whenUpdatingATopic_shouldUpdateItsDescription() {
        Topic topic = new Topic("aName", "aDescription", LocalDate.now().plusDays(1), new Professor(), "STANDARD_TYPE");
        topic.setId(1L);
        Topic updatedTopic = new Topic("aName", "anotherDescription", LocalDate.now().plusDays(1), new Professor(), "STANDARD_TYPE");
        updatedTopic.setId(1L);

        Mockito.when(topicRepository.findById(1L)).thenReturn(java.util.Optional.of(topic));
        topicService.updateTopic(updatedTopic, new ArrayList<>());
        Topic savedTopic = topicService.getTopicById(1L);

        Assertions.assertEquals("anotherDescription", savedTopic.getDescription());
    }

    @Test
    void deletingATopicWithAStudent_whenDeletingATopic_shouldCallEmailSendOneTime() {
        Topic topic = new Topic("aName", "aDescription", LocalDate.now().plusDays(1), new Professor(), "STANDARD_TYPE");
        Student student = new Student("fn", "ln", "e", "p", "STUDENT_ROLE");
        topic.setId(1L);
        topic.setStudent(student);

        Mockito.when(topicRepository.findById(topic.getId())).thenReturn(java.util.Optional.of(topic));
        topicService.deleteTopicById(topic.getId());

        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void deletingATopicWithTwoCandidates_whenDeletingATopic_shouldCallEmailSendTwoTime() {
        Topic topic = new Topic("aName", "aDescription", LocalDate.now().plusDays(1), new Professor(), "STANDARD_TYPE");
        Student student1 = new Student("fn1", "ln1", "e1", "p1", "STUDENT_ROLE");
        Student student2 = new Student("fn2", "ln2", "e2", "p2", "STUDENT_ROLE");
        topic.getCandidates().add(student1);
        topic.getCandidates().add(student2);
        topic.setId(1L);

        Mockito.when(topicRepository.findById(topic.getId())).thenReturn(java.util.Optional.of(topic));
        topicService.deleteTopicById(topic.getId());

        Mockito.verify(emailService, Mockito.times(2)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void deletingATopicOfferedToAStudent_whenDeletingATopic_shouldCallEmailSendOneTime() {
        Topic topic = new Topic("aName", "aDescription", LocalDate.now().plusDays(1), new Professor(), "STANDARD_TYPE");
        Student student = new Student("fn", "ln", "e", "p", "STUDENT_ROLE");
        topic.setStudent(student);
        topic.setAcceptanceDeadline(LocalDateTime.now().plusDays(1));
        topic.setId(1L);

        Mockito.when(topicRepository.findById(topic.getId())).thenReturn(java.util.Optional.of(topic));
        topicService.deleteTopicById(topic.getId());

        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void acceptingACandidate_whenTheTopicHasTwoCandidates_shouldCallEmailSendTwoTimesAndSetTheStudentToTheTopic() {
        Topic topic = new Topic("aName", "aDescription", LocalDate.now().plusDays(1), new Professor(), "STANDARD_TYPE");
        Student student1 = new Student("fn1", "ln1", "e1", "p1", "STUDENT_ROLE");
        Student student2 = new Student("fn2", "ln2", "e2", "p2", "STUDENT_ROLE");
        topic.getCandidates().add(student1);
        topic.getCandidates().add(student2);

        topicService.acceptCandidate(topic, student1);

        Mockito.verify(emailService, Mockito.times(2)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
        Assertions.assertEquals("fn1", topic.getStudent().getFirstName());
    }

}
