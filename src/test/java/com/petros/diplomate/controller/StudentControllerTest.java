package com.petros.diplomate.controller;

import com.petros.diplomate.model.Professor;
import com.petros.diplomate.model.Student;
import com.petros.diplomate.model.Topic;
import com.petros.diplomate.model.User;
import com.petros.diplomate.service.SelectedTagService;
import com.petros.diplomate.service.TopicService;
import com.petros.diplomate.service.UploadedFileService;
import com.petros.diplomate.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TopicService topicService;

    @MockBean
    private SelectedTagService selectedTagService;

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
        User user = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        user.setId(1L);
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                                                        MediaType.TEXT_PLAIN_VALUE, "...".getBytes());

        MockMultipartFile grades = new MockMultipartFile("grades", "grades.pdf",
                                                        MediaType.TEXT_PLAIN_VALUE, "...".getBytes());

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);

        mockMvc.perform(multipart("/api/student/{id}/profile/update-info", user.getId())
                        .file("image", image.getBytes())
                        .file("grades", grades.getBytes())
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/student/" + user.getId() + "/profile"));
    }

    @Test
    void providingInvalidParams_whenUpdatingProfileInfo_shouldRedirectToUpdateProfileInfoPage() throws Exception {
        User user = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        user.setId(1L);
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                                                        MediaType.TEXT_PLAIN_VALUE, "...".getBytes());

        MockMultipartFile grades = new MockMultipartFile("grades", "grades.pdf",
                                                         MediaType.TEXT_PLAIN_VALUE, "...".getBytes());

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(userService).updateStudentInfo(any(Student.class), anyObject());

        mockMvc.perform(multipart("/api/student/{id}/profile/update-info", user.getId())
                        .file("image", image.getBytes())
                        .file("grades", grades.getBytes())
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/student/" + user.getId() + "/profile/update-info"));
    }

    @Test
    void askingForTopicOffer_whenThereIsOpenTopicAvailable_shouldReturnTopicDetailsPage() throws Exception {
        User user = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        user.setId(1L);
        Topic topic = new Topic("name", "description", LocalDate.now().plusDays(1), new Professor(), "OPEN_TOPIC");

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.when(topicService.offerOpenTopic(any(Student.class))).thenReturn(topic);

        mockMvc.perform(get("/api/student/{id}/topics/offer-topic", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("topic-details.html"));
    }

    @Test
    void askingForTopicOffer_whenThereIsNoOpenTopicAvailable_shouldRedirectToTopicPage() throws Exception {
        User user = new Student("first", "last", "user@mail.com", "password", "STUDENT_ROLE");
        user.setId(1L);

        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
        Mockito.when(userService.loadUserByUsername("user@mail.com")).thenReturn(user);
        Mockito.doThrow(new IllegalStateException()).when(topicService).offerOpenTopic(any(Student.class));

        mockMvc.perform(get("/api/student/{id}/topics/offer-topic", user.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/student/" + user.getId() + "/topics"));
    }

}
