package com.petros.diplomate.service;

import com.petros.diplomate.model.*;
import com.petros.diplomate.repository.SessionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @MockBean
    private EmailService emailService;

    @MockBean
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionService sessionService;


    @Test
    void schedulingInterviewSession_whenEverythingIsOk_shouldCallSendEmailOneTime() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor, "STANDARD_TYPE");
        InterviewSession session = new InterviewSession("INTERVIEW_TYPE", LocalDate.now().plusDays(1), LocalTime.now(),
                                                        professor, student, topic);

        sessionService.scheduleInterviewSession(session);

        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void schedulingInterviewSession_whenInterviewIsAlreadyScheduled_shouldThrowException() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor, "STANDARD_TYPE");
        InterviewSession session = new InterviewSession("INTERVIEW_TYPE", LocalDate.now().plusDays(1), LocalTime.parse("10:00"),
                professor, student, topic);

        List<Session> allInterviewSessions = new ArrayList<>();
        allInterviewSessions.add(session);

        Mockito.when(sessionService.getAllInterviewSessions()).thenReturn(allInterviewSessions);
        IllegalStateException exception = null;
        try {
            sessionService.scheduleInterviewSession(session);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("This interview has already been scheduled for " + LocalDate.now().plusDays(1) + " at " + LocalTime.parse("10:00"),
                                exception.getMessage());
    }

    @Test
    void cancelingAnInterviewSession_shouldCallEmailSendAndDeleteByIdOneTimeEach() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor, "STANDARD_TYPE");
        InterviewSession session = new InterviewSession("INTERVIEW_TYPE", LocalDate.now().plusDays(1), LocalTime.parse("10:00"),
                professor, student, topic);

        session.setId(1L);

        sessionService.cancelSession(session);

        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
        Mockito.verify(sessionRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

    @Test
    void schedulingAMeetingSessionWithTwoStudents_whenEverythingIsOk_shouldCallSendEmailTwoTimes() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        Student student1 = new Student("sfn1", "sln1", "se1", "sp1", "STUDENT_ROLE");
        Student student2 = new Student("sfn2", "sln2", "se2", "sp2", "STUDENT_ROLE");
        MeetingSession session = new MeetingSession("MEETING_TYPE", LocalDate.now().plusDays(1), LocalTime.now(), professor);
        session.getInvitedStudents().add(student1);
        session.getInvitedStudents().add(student2);

        sessionService.scheduleMeetingSession(session);

        Mockito.verify(emailService, Mockito.times(2)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void cancelingAMeetingSessionWithTwoParticipants_shouldCallEmailSendTwoTimesAndDeleteByIdOneTime() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        Student student1 = new Student("sfn1", "sln1", "se1", "sp1", "STUDENT_ROLE");
        Student student2 = new Student("sfn2", "sln2", "se2", "sp2", "STUDENT_ROLE");
        MeetingSession session = new MeetingSession("MEETING_TYPE", LocalDate.now().plusDays(1), LocalTime.now(), professor);
        session.getInvitedStudents().add(student1);
        session.getInvitedStudents().add(student2);
        session.setId(1L);

        sessionService.cancelSession(session);

        Mockito.verify(emailService, Mockito.times(2)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
        Mockito.verify(sessionRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

    @Test
    void schedulingAnExaminationSession_whenEverythingIsOk_shouldCallSendEmailThreeTimes() {
        Professor professor1 = new Professor("pfn1", "pln1", "pe1", "pp1", "PROFESSOR_ROLE");
        Professor professor2 = new Professor("pfn2", "pln2", "pe2", "pp2", "PROFESSOR_ROLE");
        Professor professor3 = new Professor("pfn3", "pln3", "pe3", "pp3", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor1, "STANDARD_TYPE");
        ExaminationSession session = new ExaminationSession("EXAMINATION_TYPE", LocalDate.now().plusDays(1), LocalTime.now(),
                                                            professor1, student, topic);

        session.getInvitedProfessors().add(professor2);
        session.getInvitedProfessors().add(professor3);

        sessionService.scheduleExaminationSession(session);

        Mockito.verify(emailService, Mockito.times(3)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    void schedulingAnExaminationSession_whenExaminationIsAlreadyScheduled_shouldThrowException() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        professor.setId(1L);
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        student.setId(2L);
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor, "STANDARD_TYPE");
        ExaminationSession session = new ExaminationSession("INTERVIEW_TYPE", LocalDate.now().plusDays(1), LocalTime.parse("10:00"),
                                                            professor, student, topic);

        List<Session> allExaminationSessions = new ArrayList<>();
        allExaminationSessions.add(session);

        Mockito.when(sessionService.getAllExaminationSessions()).thenReturn(allExaminationSessions);
        IllegalStateException exception = null;
        try {
            sessionService.scheduleExaminationSession(session);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("This examination has already been scheduled for " + LocalDate.now().plusDays(1) + " at " + LocalTime.parse("10:00"),
                                exception.getMessage());
    }

    @Test
    void cancelingAnExaminationSession_shouldCallEmailSendTreeTimesAndDeleteByIdOneTime() {
        Professor professor1 = new Professor("pfn1", "pln1", "pe1", "pp1", "PROFESSOR_ROLE");
        Professor professor2 = new Professor("pfn2", "pln2", "pe2", "pp2", "PROFESSOR_ROLE");
        Professor professor3 = new Professor("pfn3", "pln3", "pe3", "pp3", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor1, "STANDARD_TYPE");
        ExaminationSession session = new ExaminationSession("EXAMINATION_TYPE", LocalDate.now().plusDays(1), LocalTime.now(),
                professor1, student, topic);

        session.getInvitedProfessors().add(professor2);
        session.getInvitedProfessors().add(professor3);
        session.setId(1L);

        sessionService.cancelSession(session);

        Mockito.verify(emailService, Mockito.times(3)).send(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
        Mockito.verify(sessionRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

}
