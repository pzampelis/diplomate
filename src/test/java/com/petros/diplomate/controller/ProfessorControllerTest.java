package com.petros.diplomate.controller;

import com.petros.diplomate.model.*;
import com.petros.diplomate.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProfessorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TopicService topicService;

    @MockBean
    private SelectedTagService selectedTagService;

    @MockBean
    private RequiredCourseService requiredCourseService;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private UploadedFileService uploadedFileService;

    @Mock
    private Authentication auth;


    @BeforeEach
    public void initSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void providingValidParams_whenUpdatingProfileInfo_shouldRedirectToProfilePage() throws Exception {
        User user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                MediaType.TEXT_PLAIN_VALUE, "...".getBytes());

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(multipart("/api/professor/{id}/profile/update-info", user.getId())
                        .file("image", image.getBytes())
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/profile"));
    }

    @Test
    void providingInvalidParams_whenUpdatingProfileInfo_shouldRedirectToUpdateProfileInfoPage() throws Exception {
        User user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                MediaType.TEXT_PLAIN_VALUE, "...".getBytes());

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(userService).updateProfessorInfo(any(Professor.class));

        mockMvc.perform(multipart("/api/professor/{id}/profile/update-info", user.getId())
                        .file("image", image.getBytes())
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/profile/update-info"));
    }

    @Test
    void providingValidParams_whenAddingTopic_shouldRedirectToTopicsPage() throws Exception {
        Professor user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), user, "STANDARD_TOPIC");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.when(topicService.createTopic(any(Topic.class), anyString(),any(Professor.class),anyObject())).thenReturn(topic);

        mockMvc.perform(post("/api/professor/{id}/topics/add-topic", user.getId())
                        .flashAttr("topic", topic)
                        .flashAttr("topicType", "STANDARD_TOPIC"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/topics"));
    }

    /*@Test
    void providingInvalidParams_whenAddingStandardTopic_shouldRedirectToAddStandardTopicPage() throws Exception {
        Professor user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), user, "STANDARD_TOPIC");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.when(topicService.createTopic(any(Topic.class), anyString(),any(Professor.class), anyList()))
                .thenThrow(new IllegalStateException());

        mockMvc.perform(post("/api/professor/{id}/topics/add-topic", user.getId())
                        .flashAttr("topic", topic)
                        .flashAttr("topicType", "STANDARD_TOPIC"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/topics/add-standard-topic"));
    }

    @Test
    void providingInvalidParams_whenAddingOpenTopic_shouldRedirectToAddOpenTopicPage() throws Exception {
        Professor user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), user, "OPEN_TOPIC");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.when(topicService.createTopic(any(Topic.class), anyString(),any(Professor.class), anyObject()))
                .thenThrow(new IllegalStateException());

        mockMvc.perform(post("/api/professor/{id}/topics/add-topic", user.getId())
                        .flashAttr("topic", topic)
                        .flashAttr("topicType", "OPEN_TOPIC"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/topics/add-open-topic"));
    }*/

    @Test
    void providingValidParams_whenUpdatingTopic_shouldRedirectToTopicsPage() throws Exception {
        Professor user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), user, "STANDARD_TOPIC");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(post("/api/professor/{id}/topics/update-topic", user.getId())
                        .flashAttr("topic", topic))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/topics"));
    }

    @Test
    void providingInvalidParams_whenUpdatingTopic_shouldRedirectToUpdateTopicPage() throws Exception {
        Professor user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), user, "STANDARD_TOPIC");
        topic.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(topicService).updateTopic(any(Topic.class), anyObject());

        mockMvc.perform(post("/api/professor/{id}/topics/update-topic", user.getId())
                        .flashAttr("topic", topic))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/topics/update-topic"));
    }

    @Test
    void providingValidParams_whenSchedulingInterview_shouldRedirectToTopicCandidatesPage() throws Exception {
        Professor professor = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        Student student = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), professor, "STANDARD_TOPIC");
        professor.setId(1L);
        student.setId(2L);
        topic.setId(1L);
        Session session = new InterviewSession("INTERVIEW_TYPE", LocalDate.now(), LocalTime.now(), professor, student, topic);
        session.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(professor);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(professor);
        Mockito.when(userService.getUserById(2L)).thenReturn(student);
        Mockito.when(topicService.getTopicById(1L)).thenReturn(topic);

        mockMvc.perform(post("/api/professor/{id}/calendar/interview-session", professor.getId())
                        .param("studentId", String.valueOf(student.getId()))
                        .param("topicId", String.valueOf(topic.getId()))
                        .param("date", "2016-01-06")
                        .param("time", "16:16")
                        .param("sessionType", "INTERVIEW_TYPE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + professor.getId() + "/topics/topic-candidates"));
    }

    @Test
    void providingInvalidParams_whenSchedulingInterview_shouldRedirectToScheduleInterviewPage() throws Exception {
        Professor professor = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        Student student = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), professor, "STANDARD_TOPIC");
        professor.setId(1L);
        student.setId(2L);
        topic.setId(1L);
        Session session = new InterviewSession("INTERVIEW_TYPE", LocalDate.now(), LocalTime.now(), professor, student, topic);
        session.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(professor);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(professor);
        Mockito.when(userService.getUserById(2L)).thenReturn(student);
        Mockito.when(topicService.getTopicById(1L)).thenReturn(topic);
        Mockito.doThrow(new IllegalStateException()).when(sessionService).scheduleInterviewSession(any(InterviewSession.class));

        mockMvc.perform(post("/api/professor/{id}/calendar/interview-session", professor.getId())
                        .param("studentId", String.valueOf(student.getId()))
                        .param("topicId", String.valueOf(topic.getId()))
                        .param("date", "2016-01-06")
                        .param("time", "16:16")
                        .param("sessionType", "INTERVIEW_TYPE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + professor.getId() + "/calendar/interview-session"));
    }

    @Test
    void providingValidParams_whenSchedulingMeeting_shouldRedirectToCalendarPage() throws Exception {
        Professor professor = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        Student student = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), professor, "STANDARD_TOPIC");
        professor.setId(1L);
        student.setId(2L);
        topic.setId(1L);
        Session session = new MeetingSession("MEETING_TYPE", LocalDate.now(), LocalTime.now(), professor);
        session.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(professor);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(professor);
        Mockito.when(userService.getUserById(2L)).thenReturn(student);
        Mockito.when(topicService.getTopicById(1L)).thenReturn(topic);

        mockMvc.perform(post("/api/professor/{id}/calendar/meeting-session", professor.getId())
                        .param("studentId", String.valueOf(student.getId()))
                        .param("topicId", String.valueOf(topic.getId()))
                        .param("date", "2016-01-06")
                        .param("time", "16:16")
                        .param("sessionType", "MEETING_TYPE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + professor.getId() + "/calendar"));
    }

    @Test
    void providingInvalidParams_whenSchedulingMeeting_shouldRedirectToScheduleMeetingPage() throws Exception {
        Professor professor = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        Student student = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), professor, "STANDARD_TOPIC");
        professor.setId(1L);
        student.setId(2L);
        topic.setId(1L);
        Session session = new MeetingSession("MEETING_TYPE", LocalDate.now(), LocalTime.now(), professor);
        session.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(professor);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(professor);
        Mockito.when(userService.getUserById(2L)).thenReturn(student);
        Mockito.when(topicService.getTopicById(1L)).thenReturn(topic);
        Mockito.doThrow(new IllegalStateException()).when(sessionService).scheduleMeetingSession(any(MeetingSession.class));

        mockMvc.perform(post("/api/professor/{id}/calendar/meeting-session", professor.getId())
                        .param("studentId", String.valueOf(student.getId()))
                        .param("topicId", String.valueOf(topic.getId()))
                        .param("date", "2016-01-06")
                        .param("time", "16:16")
                        .param("sessionType", "MEETING_TYPE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + professor.getId() + "/calendar/meeting-session"));
    }

    @Test
    void providingValidParams_whenUpdatingSession_shouldRedirectToCalendarPage() throws Exception {
        Professor user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        Session session = new InterviewSession("INTERVIEW_TYPE", LocalDate.now(), LocalTime.now(), user, new Student(), new Topic());
        session.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.when(sessionService.getSessionById(1L)).thenReturn(session);

        mockMvc.perform(post("/api/professor/{id}/calendar/update-session", user.getId())
                        .param("date", "2016-01-06")
                        .param("time", "16:16")
                        .param("sessionId", String.valueOf(session.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/calendar"));
    }

    @Test
    void providingInvalidParams_whenUpdatingSession_shouldRedirectToUpdateSessionPage() throws Exception {
        Professor user = new Professor("first", "last", "user@mail.com", "password", "PROFESSOR_ROLE");
        user.setId(1L);
        Session session = new InterviewSession("INTERVIEW_TYPE", LocalDate.now(), LocalTime.now(), user, new Student(), new Topic());
        session.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.when(sessionService.getSessionById(1L)).thenReturn(session);
        Mockito.doThrow(new IllegalStateException()).when(sessionService).rescheduleInterview(any(InterviewSession.class),
                                                                                              anyObject(), anyObject());

        mockMvc.perform(post("/api/professor/{id}/calendar/update-session", user.getId())
                        .param("date", "2016-01-06")
                        .param("time", "16:16")
                        .param("sessionId", String.valueOf(session.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/professor/" + user.getId() + "/calendar/update-session"));
    }

}
