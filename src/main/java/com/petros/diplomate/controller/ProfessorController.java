package com.petros.diplomate.controller;

import com.petros.diplomate.model.*;
import com.petros.diplomate.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/api/professor/{id}")
public class ProfessorController {

    @Autowired
    private UserService userService;

    @Autowired
    private UploadedFileService uploadedFileService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private RequiredCourseService requiredCourseService;

    @Autowired
    private SelectedTagService selectedTagService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ExaminationScoreService examinationScoreService;


    @PostMapping("/profile/update-info")
    public String processProfessorUpdateInfoForm(@ModelAttribute("user") Professor updatedUser,
                                                 @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                                 @RequestParam(value = "imageRemoval", required=false) String imageRemoval,
                                                 RedirectAttributes redirectAttributes) throws IOException {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        try {
            if (!imageFile.isEmpty()) {
                String fileName = "user" + currentUser.getId() + "-profile-image.png";

                updatedUser.setProfileImagePath("/uploads/profile-images/" + fileName);
            }
            else
                updatedUser.setProfileImagePath(currentUser.getProfileImagePath());

            if (imageRemoval != null) {
                String fileName = "user" + currentUser.getId() + "-profile-image.png";
                uploadedFileService.deleteFile("image", fileName);
                updatedUser.setProfileImagePath("/assets/images/default-profile-picture.png");
            }

            userService.updateProfessorInfo(updatedUser);

            if (!imageFile.isEmpty()) {
                String fileName = "user" + currentUser.getId() + "-profile-image."
                        + StringUtils.getFilenameExtension(imageFile.getOriginalFilename());

                uploadedFileService.uploadFile(imageFile, "image", fileName);
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/api/professor/" + currentUser.getId() + "/profile/update-info";
        }

        return "redirect:/api/professor/" + currentUser.getId() + "/profile";
    }

    @GetMapping("/topics")
    public String showProfessorTopicsPage(@Param("topicId") Long topicId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);
        model.addAttribute("topicId", topicId);

        List<Topic> availableStandardTopics = topicService.getAvailableActiveStandardTopicsByProfessor(currentUser);
        model.addAttribute("availableStandardTopics", availableStandardTopics);

        List<Topic> availableOpenTopics = topicService.getAvailableActiveOpenTopicsByProfessor(currentUser);
        model.addAttribute("availableOpenTopics", availableOpenTopics);

        List<Topic> takenTopics =topicService.getTakenActiveTopicsByProfessorId(currentUser.getId());
                model.addAttribute("takenTopics", takenTopics);

        return "professor-topics.html";
    }

    @GetMapping("/topics/add-standard-topic")
    public String showAddStandardTopicForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        Topic topic = new Topic();
        requiredCourseService.initializeRequiredCourses(topic, courseService.getAllCourses());

        List<String> tags = tagService.getTagsAsStringsList();
        model.addAttribute("tags", tags);

        model.addAttribute("topic", topic);
        model.addAttribute("topicType", "STANDARD_TYPE");

        model.addAttribute("minDate", LocalDate.now().plusDays(1));

        return "add-topic.html";
    }

    @GetMapping("/topics/add-open-topic")
    public String showAddOpenTopicForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<String> tags = tagService.getTagsAsStringsList();
        model.addAttribute("tags", tags);

        Topic topic = new Topic();
        model.addAttribute("topic", topic);
        model.addAttribute("topicType", "OPEN_TYPE");

        model.addAttribute("minDate", LocalDate.now().plusDays(1));

        return "add-topic.html";
    }

    @PostMapping("/topics/add-topic")
    public String processAddTopicForm(@ModelAttribute("topic") Topic topic, @ModelAttribute("topicType") String topicType,
                                      @RequestParam(value = "selectedTags", required=false) List<String> selectedTags,
                                      RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        try {
            Topic createdTopic = topicService.createTopic(topic, topicType, currentUser, selectedTags);

            if (topicType.equals("STANDARD_TYPE")) {
                requiredCourseService.addRequiredCourses(createdTopic, topic.getRequiredCourses());
                List<Student> studentsWithoutTopic = userService.getStudentsWithoutTopic();
                topicService.notifyPossiblyInterestedStudents(topic.getName(), studentsWithoutTopic);
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            switch (topicType) {
                case "STANDARD_TYPE":
                    return "redirect:/api/professor/" + currentUser.getId() + "/topics/add-standard-topic";
                case "OPEN_TYPE":
                    return "redirect:/api/professor/" + currentUser.getId() + "/topics/add-open-topic";
            }
        }

        return "redirect:/api/professor/" + currentUser.getId() + "/topics";
    }

    @GetMapping("/topics/old")
    public String showOldTopicsPage(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<Topic> oldTopics = topicService.getOldTopicsByProfessorId(currentUser.getId());
        model.addAttribute("oldTopics", oldTopics);

        return "old-topics.html";
    }

    @GetMapping("/topics/update-topic")
    public String showUpdateTopicForm(@RequestParam("topicId") Long topicId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        Topic topic = topicService.getTopicById(topicId);
        requiredCourseService.updateRequiredCoursesIfNeeded(topic);
        List<Tag> allTags = tagService.getAllTags();
        selectedTagService.updateSelectedTopicTagsIfNeeded(topic, allTags);

        model.addAttribute("topic", topic);

        model.addAttribute("minDate", LocalDate.now().plusDays(1));

        return "update-topic.html";
    }

    @PostMapping("/topics/update-topic")
    public String processUpdateTopicForm(@ModelAttribute("topic") Topic updatedTopic, RedirectAttributes redirectAttributes,
                                         @RequestParam(value = "selectedTags", required=false) List<String> updatedSelectedTags) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        try {
            topicService.updateTopic(updatedTopic, updatedSelectedTags);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("topicId", updatedTopic.getId());

            return "redirect:/api/professor/" + currentUser.getId() + "/topics/update-topic";
        }

        return "redirect:/api/professor/" + currentUser.getId() + "/topics";
    }

    @GetMapping("/topics/topic-candidates")
    public String showTopicCandidatesPage(@Param("studentId") Long studentId, @RequestParam("topicId") Long topicId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);
        model.addAttribute("studentId", studentId);

        Topic topic = topicService.getTopicById(topicId);
        model.addAttribute("topic", topic);

        List<Student> uninvitedStudents = topicService.getUninvitedCandidates(topic);
        model.addAttribute("uninvitedStudents", uninvitedStudents);

        List<InterviewSession> upcomingInterviews = sessionService.getUpcomingInterviewsByTopic(topic);
        model.addAttribute("upcomingInterviews", upcomingInterviews);

        return "topic-candidates.html";
    }

    @GetMapping("/topics/topic-completed-interviews")
    public String showTopicCompletedInterviewsPage(@RequestParam("topicId") Long topicId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        Topic topic = topicService.getTopicById(topicId);
        model.addAttribute("topic", topic);

        List<InterviewSession> unratedInterviews = sessionService.getUnratedPassedInterviewSessionsByTopic(topic);
        model.addAttribute("unratedInterviews", unratedInterviews);

        List<InterviewSession> ratedInterviews = sessionService.getRatedPassedInterviewSessionsByTopic(topic);
        model.addAttribute("ratedInterviews", ratedInterviews);

        return "topic-completed-interviews.html";
    }

    @PostMapping("/topics/rate-interview")
    public String processRateInterviewForm(@RequestParam("topicId") Long topicId, @RequestParam("interviewId") Long interviewId,
                                           @RequestParam("score") Double score, RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        redirectAttributes.addAttribute("topicId", topicId);

        InterviewSession interview = (InterviewSession) sessionService.getSessionById(interviewId);
        sessionService.rateInterview(interview, score);

        return "redirect:/api/professor/" + currentUser.getId() + "/topics/topic-completed-interviews";
    }

    @PostMapping("/topics/accept-candidate")
    public String processAcceptCandidateRequest(@RequestParam("studentId") Long studentId, @RequestParam("topicId") Long topicId) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        Student student = (Student) userService.getUserById(studentId);
        Topic topic = topicService.getTopicById(topicId);

        topicService.acceptCandidate(topic, student);
        sessionService.removeInterviewSessionsByTopic(topic);

        return "redirect:/api/professor/" + currentUser.getId() + "/topics";
    }

    @PostMapping("/topics/delete-topic")
    public String processDeleteTopicRequest(@RequestParam("topicId") Long topicId) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        topicService.deleteTopicById(topicId);

        return "redirect:/api/professor/" + currentUser.getId() + "/topics";
    }

    @GetMapping("/calendar")
    public String showProfessorCalendarPage(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<Session> upcomingInterviews =
                sessionService.getUpcomingSessionsByTypeAndProfessor("INTERVIEW_TYPE", currentUser);
        model.addAttribute("interviews", upcomingInterviews);

        List<Session> upcomingMeetings =
                sessionService.getUpcomingSessionsByTypeAndProfessor("MEETING_TYPE", currentUser);
        model.addAttribute("meetings", upcomingMeetings);

        List<Session> upcomingExaminations =
                sessionService.getUpcomingSessionsByTypeAndProfessor("EXAMINATION_TYPE", currentUser);
        model.addAttribute("myExams", upcomingExaminations);

        List<ExaminationSession> upcomingOthersExaminations =
                sessionService.getUpcomingOthersExaminations(sessionService.getExaminationSessionsProfessorIsInvitedTo(currentUser));
        model.addAttribute("othersExams", upcomingOthersExaminations);

        return "calendar.html";
    }

    @GetMapping("/calendar/interview-session")
    public String showScheduleInterviewSessionForm(@RequestParam("studentId") Long studentId,
                                                   @RequestParam("topicId") Long topicId, Model model) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        Student student = (Student) userService.getUserById(studentId);
        model.addAttribute("student", student);

        Topic topic = topicService.getTopicById(topicId);
        model.addAttribute("topic", topic);

        model.addAttribute("minDate", LocalDate.now());

        model.addAttribute("sessionType", "INTERVIEW_TYPE");

        return "schedule-session.html";
    }

    @PostMapping("/calendar/interview-session")
    public String processScheduleInterviewSessionForm(@RequestParam("studentId") Long studentId, @RequestParam("topicId") Long topicId,
                                                      @RequestParam("date") String date, @RequestParam("time") String time,
                                                      @RequestParam("sessionType") String sessionType,
                                                      RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        redirectAttributes.addAttribute("topicId", topicId);

        InterviewSession session = new InterviewSession(sessionType, LocalDate.parse(date), LocalTime.parse(time),
                                                        currentUser, (Student) userService.getUserById(studentId),
                                                        topicService.getTopicById(topicId));

        try {
            sessionService.scheduleInterviewSession(session);
            redirectAttributes.addFlashAttribute("message", "Interview scheduled successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addAttribute("studentId", studentId);
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/api/professor/" + currentUser.getId() + "/calendar/interview-session";
        }

        return "redirect:/api/professor/" + currentUser.getId() + "/topics/topic-candidates";
    }

    @GetMapping("/calendar/meeting-session")
    public String showScheduleMeetingSessionForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<Student> students = topicService.getStudentsByProfessor(currentUser);
        model.addAttribute("students", students);

        model.addAttribute("sessionType", "MEETING_TYPE");

        model.addAttribute("minDate", LocalDate.now());

        return "schedule-session.html";
    }

    @PostMapping("/calendar/meeting-session")
    public String processScheduleMeetingSessionForm(@RequestParam("studentId") Long studentId,
                                                    @RequestParam("date") String date, @RequestParam("time") String time,
                                                    @RequestParam("sessionType") String sessionType,
                                                    RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        MeetingSession session = new MeetingSession(sessionType, LocalDate.parse(date),
                                                             LocalTime.parse(time), currentUser);

        if (studentId == 0)
            for (Student student : topicService.getStudentsByProfessor(currentUser))
                session.getInvitedStudents().add(student);
        else
            session.getInvitedStudents().add((Student) userService.getUserById(studentId));

        try {
            sessionService.scheduleMeetingSession(session);
            redirectAttributes.addFlashAttribute("message", "Meeting scheduled successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/api/professor/" + currentUser.getId() + "/calendar/meeting-session";
        }

        return "redirect:/api/professor/" + currentUser.getId() + "/calendar";
    }

    @GetMapping("/calendar/examination-session")
    public String showScheduleExaminationSessionForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<Student> students = topicService.getStudentsByProfessor(currentUser);
        model.addAttribute("students", students);

        List<User> professors = userService.getAllProfessors();
        model.addAttribute("professors", professors);

        model.addAttribute("minDate", LocalDate.now());

        model.addAttribute("sessionType", "EXAMINATION_TYPE");

        return "schedule-session.html";
    }

    @PostMapping("/calendar/examination-session")
    public String processScheduleExaminationSessionForm(@RequestParam("professorId1") Long professorId1,
                                                        @RequestParam("professorId2") Long professorId2,
                                                        @RequestParam("studentId") Long studentId,
                                                        @RequestParam("date") String date, @RequestParam("time") String time,
                                                        @RequestParam("sessionType") String sessionType,
                                                        RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        Student student = (Student) userService.getUserById(studentId);
        Topic topic = topicService.getTopicByStudentId(studentId).get();

        if (!sessionService.isProfessorsSelectionValid(currentUser.getId(), professorId1, professorId2)) {
            redirectAttributes.addFlashAttribute("error", "Professors selected are invalid");

            redirectAttributes.addAttribute("studentId", studentId);
            redirectAttributes.addAttribute("topicId", topic.getId());

            return "redirect:/api/professor/" + currentUser.getId() + "/calendar/examination-session";
        }

        ExaminationSession session = new ExaminationSession(sessionType, LocalDate.parse(date), LocalTime.parse(time),
                                                                     currentUser, student, topic);

        session.getInvitedProfessors().add((Professor) userService.getUserById(professorId1));
        session.getInvitedProfessors().add((Professor) userService.getUserById(professorId2));

        try {
            sessionService.scheduleExaminationSession(session);
            redirectAttributes.addFlashAttribute("message", "Examination scheduled successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/api/professor/" + currentUser.getId() + "/calendar/examination-session";
        }

        return "redirect:/api/professor/" + currentUser.getId() + "/calendar";
    }

    @GetMapping("/calendar/update-session")
    public String showRescheduleSessionForm(@RequestParam("sessionId") Long sessionId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        Session session = sessionService.getSessionById(sessionId);
        model.addAttribute("session", session);
        model.addAttribute("sessionId", sessionId);

        model.addAttribute("minDate", LocalDate.now());

        return "update-session.html";
    }

    @PostMapping("/calendar/update-session")
    public String processRescheduleSessionForm(@RequestParam("date") String date, @RequestParam("time") String time,
                                               @RequestParam("sessionId") Long sessionId,
                                               RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        Session session = sessionService.getSessionById(sessionId);

        try {
            sessionService.checkDateAndTimeValidity(LocalDate.parse(date), LocalTime.parse(time));
            if (session.getType().equals("INTERVIEW_TYPE")) {
                sessionService.rescheduleInterview((InterviewSession) session, LocalDate.parse(date),
                                                               LocalTime.parse(time));
                redirectAttributes.addFlashAttribute("message", "Interview rescheduled successfully");
            }
            else if (session.getType().equals("MEETING_TYPE")) {
                sessionService.rescheduleMeeting((MeetingSession) session, LocalDate.parse(date),
                                                           LocalTime.parse(time));
                redirectAttributes.addFlashAttribute("message", "Meeting rescheduled successfully");
            }
            else {
                sessionService.rescheduleExamination((ExaminationSession) session, LocalDate.parse(date),
                                                                   LocalTime.parse(time));
                redirectAttributes.addFlashAttribute("message", "Examination rescheduled successfully");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("sessionId", sessionId);

            return "redirect:/api/professor/" + currentUser.getId() + "/calendar/update-session";
        }

        return "redirect:/api/professor/" + currentUser.getId() + "/calendar";
    }

    @PostMapping("/calendar/cancel-session")
    public String processCancelSessionRequest(@RequestParam("sessionId") Long sessionId, RedirectAttributes redirectAttributes) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        Session session = sessionService.getSessionById(sessionId);
        sessionService.cancelSession(session);

        redirectAttributes.addFlashAttribute("message", "Session canceled successfully");

        return "redirect:/api/professor/" + currentUser.getId() + "/calendar";
    }

    @GetMapping("/calendar/unrated-examinations")
    public String showUnratedExaminationsPage(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<ExaminationSession> unratedExaminations = sessionService.getUnratedExaminationsByProfessor(currentUser);
        model.addAttribute("unratedExaminations", unratedExaminations);

        return "unrated-examinations.html";
    }

    @PostMapping("/calendar/rate-examination")
    public String processRateExaminationForm(@RequestParam("topicId") Long topicId,
                                             @RequestParam("presentationScore") Double presentationScore,
                                             @RequestParam("textScore") Double textScore,
                                             @RequestParam("methodologyScore") Double methodologyScore,
                                             @RequestParam(value = "preparationScore", required = false) Double preparationScore) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Professor currentUser = (Professor) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        Topic topic = topicService.getTopicById(topicId);
        examinationScoreService.rateExamination(currentUser, topic, presentationScore, textScore, methodologyScore, preparationScore);
        examinationScoreService.completeExaminationScoring(topic);

        return "redirect:/api/professor/" + currentUser.getId() + "/calendar/unrated-examinations";
    }

}
