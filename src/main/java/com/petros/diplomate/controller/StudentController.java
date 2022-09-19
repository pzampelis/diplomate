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

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/student/{id}")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private UploadedFileService uploadedFileService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TagService tagService;

    @Autowired
    private SelectedTagService selectedTagService;


    @PostMapping("/profile/update-info")
    public String processStudentUpdateInfoForm(@ModelAttribute("user") Student updatedUser,
                                               @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                               @RequestParam(value = "imageRemoval", required=false) String imageRemoval,
                                               @RequestParam(value = "grades", required = false) MultipartFile gradesFile,
                                               @RequestParam(value = "pdfRemoval", required=false) String pdfRemoval,
                                               @RequestParam(value="selectedTags", required = false) List<String> updatedSelectedTags,
                                               RedirectAttributes redirectAttributes) throws IOException {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());

        try {
            if (!imageFile.isEmpty()) {
                String fileName = "user" + currentUser.getId() + "-profile-image.png";

                updatedUser.setProfileImagePath("/uploads/profile-images/" + fileName);
            }
            else
                updatedUser.setProfileImagePath(currentUser.getProfileImagePath());

            if (!gradesFile.isEmpty()) {
                String fileName = "user" + currentUser.getId() + "-grades-pdf."
                                  + StringUtils.getFilenameExtension(gradesFile.getOriginalFilename());

                updatedUser.setGradesPDFPath("/uploads/grades-pdfs/" + fileName);
            }
            else
                updatedUser.setGradesPDFPath(currentUser.getGradesPDFPath());

            if (pdfRemoval != null) {
                String fileName = "user" + currentUser.getId() + "-grades-pdf.pdf";
                uploadedFileService.deleteFile("gradesPDF", fileName);
                updatedUser.setGradesPDFPath("");
            }

            if (imageRemoval != null) {
                String fileName = "user" + currentUser.getId() + "-profile-image.png";
                uploadedFileService.deleteFile("image", fileName);
                updatedUser.setProfileImagePath("/assets/images/default-profile-picture.png");
            }

            userService.updateStudentInfo(updatedUser, updatedSelectedTags);

            if (!imageFile.isEmpty()) {
                String fileName = "user" + currentUser.getId() + "-profile-image."
                                  + StringUtils.getFilenameExtension(imageFile.getOriginalFilename());

                uploadedFileService.uploadFile(imageFile, "image", fileName);
            }

            if (!gradesFile.isEmpty()) {
                String fileName = "user" + currentUser.getId() + "-grades-pdf."
                                  + StringUtils.getFilenameExtension(gradesFile.getOriginalFilename());

                uploadedFileService.uploadFile(gradesFile, "gradesPDF", fileName);
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/api/student/" + currentUser.getId() + "/profile/update-info";
        }

        return "redirect:/api/student/" + currentUser.getId() + "/profile";
    }

    @GetMapping("/topics")
    public String showStudentTopicPage(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        boolean offeredTopicAvailable = topicService.isOfferedTopicAvailable(currentUser);
        model.addAttribute("offeredTopicAvailable", offeredTopicAvailable);
        Optional<Topic> optTopic = topicService.getTopicByStudentId(currentUser.getId());
        Topic studentTopic;
        if (optTopic.isEmpty())
            studentTopic = null;
        else
            studentTopic = optTopic.get();
        model.addAttribute("studentTopic", studentTopic);

        return "student-topics.html";
    }

    @GetMapping("/topics/search")
    public String showSearchTopicForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<User> professors = userService.getAllProfessors();
        model.addAttribute("professors", professors);

        List<String> tags = tagService.getTagsAsStringsList();
        model.addAttribute("tags", tags);

        return "search-topics.html";
    }

    @GetMapping("/topics/search-results")
    public String processSearchTopicForm(@Param("topicId") Long topicId, @Valid Long professorId, Model model,
                                         @RequestParam(value = "selectedTags", required=false) List<String> selectedTags,
                                         RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);
        model.addAttribute("topicId", topicId);

        if (selectedTags == null)
            selectedTags = new ArrayList<>();

        try {
            Professor professor = null;
            if (professorId != null)
                professor = (Professor) userService.getUserById(professorId);
            List<Topic> matchingTopics = topicService.getTopicsBySearch(professor, selectedTags);
            model.addAttribute("topics", matchingTopics);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/api/student/" + currentUser.getId() + "/topics/search";
        }

        return "search-topics-results.html";
    }

    @PostMapping("/topics/apply")
    public String processApplyAsCandidateRequest(@ModelAttribute("topic") Topic selectedTopic, RedirectAttributes redirectAttributes) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        redirectAttributes.addAttribute("topicId", selectedTopic.getId());

        Topic topic = topicService.getTopicById(selectedTopic.getId());

        try {
            topicService.addCandidate(topic, currentUser);
            redirectAttributes.addFlashAttribute("message", "application completed");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/api/student/" + currentUser.getId() + "/topics/details";
    }

    @GetMapping("/topics/applications")
    public String showTopicApplicationsPage(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        topicService.updateTopicsStatus(currentUser);

        List<Topic> topicsStudentIsInterestedIn = topicService.getTopicsStudentIsInterestedIn(currentUser);
        model.addAttribute("topicsStudentIsInterestedIn", topicsStudentIsInterestedIn);

        return "topics-applications.html";
    }

    @PostMapping("/topics/cancel-application")
    public String processCancelApplicationRequest(@RequestParam("topicId") Long topicId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        Topic topic = topicService.getTopicById(topicId);
        topicService.removeCandidate(topic, currentUser);

        return "redirect:/api/student/" + currentUser.getId() + "/topics/applications";
    }

    @GetMapping("/topics/offer-topic")
    public String showOfferedTopicPage(Model model, RedirectAttributes redirectAttributes) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        try {
            Topic topic = topicService.offerOpenTopic(currentUser);
            model.addAttribute("topic", topic);

            String selectedTopicTagsString = selectedTagService.getSelectedTopicTagsAsString(topic);
            model.addAttribute("selectedTopicTags", selectedTopicTagsString);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/api/student/" + currentUser.getId() + "/topics";
        }

        return "topic-details.html";
    }

    @PostMapping(value = "/topics/offered-topic-answer", params = "accept")
    public String processAcceptOfferedTopicRequest(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        try {
            topicService.acceptOpenTopic(currentUser);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            Topic topic = topicService.getTopicByStudentId(currentUser.getId()).get();
            model.addAttribute("topic", topic);

            String selectedTopicTagsString = selectedTagService.getSelectedTopicTagsAsString(topic);
            model.addAttribute("selectedTopicTags", selectedTopicTagsString);

            return "topic-details.html";
        }


        return "redirect:/api/student/" + currentUser.getId() + "/topics";
    }

    @PostMapping(value = "/topics/offered-topic-answer", params = "reject")
    public String processRejectOfferedTopicRequest(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        topicService.rejectOpenTopic(currentUser);

        return "redirect:/api/student/" + currentUser.getId() + "/topics";
    }

    @GetMapping("/calendar")
    public String showStudentCalendarPage(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Student currentUser = (Student) userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        List<InterviewSession> interviews = sessionService.getUpcomingInterviewsByStudent(currentUser);
        model.addAttribute("interviews", interviews);

        List<MeetingSession> meetings = sessionService.getUpcomingMeetingsByStudent(currentUser);
        model.addAttribute("meetings", meetings);

        List<ExaminationSession> myExams = new ArrayList<>();
        ExaminationSession exam = sessionService.getExaminationSessionByStudent(currentUser);
        if (exam != null && !sessionService.hasSessionPassed(exam))
            myExams.add(exam);
        model.addAttribute("myExams", myExams);

        model.addAttribute("othersExams", new ArrayList<>());

        return "calendar.html";
    }

}
