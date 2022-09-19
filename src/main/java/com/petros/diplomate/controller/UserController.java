package com.petros.diplomate.controller;

import com.petros.diplomate.model.*;
import com.petros.diplomate.service.*;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping(value={"/api/student/{id}", "/api/professor/{id}"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private TagService tagService;

    @Autowired
    private SelectedTagService selectedTagService;

    @Autowired
    private UploadedFileService uploadedFileService;


    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("visitor", currentUser);
        model.addAttribute("host", currentUser);

        if (currentUser.getRole().equals("STUDENT_ROLE")) {
            String selectedStudentTagsString = selectedTagService.getSelectedStudentTagsAsString((Student) currentUser);
            model.addAttribute("selectedStudentTags", selectedStudentTagsString);
        }

        return "profile.html";
    }

    @GetMapping("/profile/update-info")
    public String showUpdateInfoForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        if (currentUser.getRole().equals("STUDENT_ROLE")) {
            List<Topic> topicsStudentIsInterestedIn = topicService.getTopicsStudentIsInterestedIn((Student) currentUser);
            model.addAttribute("topicsStudentIsInterestedIn", topicsStudentIsInterestedIn);
            List<Tag> allTags = tagService.getAllTags();
            selectedTagService.updateSelectedStudentTagsIfNeeded((Student) currentUser, allTags);
        }

        return "update-profile.html";
    }

    @GetMapping("/profile/update-password")
    public String showUpdatePasswordForm(Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        return "update-password.html";
    }

    @PostMapping("/profile/update-password")
    public String processUpdatePasswordForm(@RequestParam("oldPassword") String oldPassword,
                                            @RequestParam("newPassword") String newPassword,
                                            @RequestParam("reenteredNewPassword") String reenteredNewPassword,
                                            RedirectAttributes redirectAttributes) {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());

        try {
            userService.changePassword(currentUser, oldPassword, newPassword, reenteredNewPassword);
            redirectAttributes.addFlashAttribute("message", "Password updated successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            if (currentUser.getRole().equals("PROFESSOR_ROLE"))
                return "redirect:/api/professor/" + currentUser.getId() + "/profile/update-password";

            return "redirect:/api/student/" + currentUser.getId() + "/profile/update-password";
        }

        if (currentUser.getRole().equals("PROFESSOR_ROLE"))
            return "redirect:/api/professor/" + currentUser.getId() + "/profile";

        return "redirect:/api/student/" + currentUser.getId() + "/profile";
    }

    @GetMapping("/profile/info")
    public String showViewedProfilePage(@RequestParam("hostId") Long hostId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("visitor", currentUser);

        User host = userService.getUserById(hostId);
        model.addAttribute("host", host);

        return "profile.html";
    }

    @PostMapping("/profile/info/download-pdf")
    public ResponseEntity<Object> processDownloadGradesPDFRequest(@RequestParam("hostId") Long hostId) throws IOException {
        Path currentRelativePath = Paths.get("");
        String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();

        File pdfFile = new File(currentAbsolutePath + "\\uploads\\grades-pdfs\\" + "user" + hostId + "-grades-pdf.pdf");

        InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));
        HttpHeaders headers = new HttpHeaders();

        headers.add("Content-Disposition", "attachment; filename=" + pdfFile.getName());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok().headers(headers).contentLength(pdfFile.length())
                .contentType(new MediaType(MediaType.parseMediaType("application/pdf"))).body(resource);
    }

    @GetMapping("/topics/details")
    public String showViewedTopicPage(@RequestParam("topicId") Long topicId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);

        Topic topic = topicService.getTopicById(topicId);
        model.addAttribute("topic", topic);

        Boolean hasRequiredCourses = topicService.hasTopicRequiredCourses(topic);
        model.addAttribute("hasRequiredCourses", hasRequiredCourses);

        String selectedTopicTagsString = selectedTagService.getSelectedTopicTagsAsString(topic);
        model.addAttribute("selectedTopicTags", selectedTopicTagsString);

        return "topic-details.html";
    }

    @GetMapping("/topic-repository")
    public String showTopicRepositoryPage(@RequestParam("topicId") Long topicId, Model model) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        model.addAttribute("user", currentUser);
        model.addAttribute("topicId", topicId);

        Topic topic = topicService.getTopicById(topicId);

        List<UploadedFile> professorFiles = uploadedFileService.getUploadedFilesByUploaderAndTopic("PROFESSOR_UPLOADER", topic);
        model.addAttribute("professorFiles", professorFiles);

        List<UploadedFile> studentFiles = uploadedFileService.getUploadedFilesByUploaderAndTopic("STUDENT_UPLOADER", topic);
        model.addAttribute("studentFiles", studentFiles);

        return "topic-repository.html";
    }

    @PostMapping("/topic-repository/add-file")
    public String processAddFileRequest(@RequestParam("topicId") Long topicId, @RequestParam("file") MultipartFile file,
                                        RedirectAttributes redirectAttributes) throws IOException {

        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());
        redirectAttributes.addAttribute("topicId", topicId);

        if (!file.isEmpty())
            try {
                String originalName = FilenameUtils.removeExtension(file.getOriginalFilename());
                String fileName = originalName + "-" + currentUser.getId() + "-" + topicId + "."
                                  + StringUtils.getFilenameExtension(file.getOriginalFilename());

                Topic topic = topicService.getTopicById(topicId);

                if (currentUser.getRole().equals("PROFESSOR_ROLE"))
                    uploadedFileService.saveNewFile(fileName, "PROFESSOR_UPLOADER", LocalDate.now(), topic);
                else if (currentUser.getRole().equals("STUDENT_ROLE"))
                    uploadedFileService.saveNewFile(fileName, "STUDENT_UPLOADER", LocalDate.now(), topic);

                uploadedFileService.uploadFile(file, "file", fileName);

            } catch (IllegalStateException e) {
                redirectAttributes.addAttribute("error", e.getMessage());
            }

        if (currentUser.getRole().equals("PROFESSOR_ROLE"))
            return "redirect:/api/professor/" + currentUser.getId() + "/topic-repository";

        return "redirect:/api/student/" + currentUser.getId() + "/topic-repository";
    }

    @PostMapping("/topic-repository/download-file")
    public ResponseEntity<Object> processDownloadFileRequest(@RequestParam("fileId") Long fileId) throws IOException {
        Path currentRelativePath = Paths.get("");
        String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();

        UploadedFile uploadedFile = uploadedFileService.getUploadedFileById(fileId);
        File requestedFile = new File(currentAbsolutePath + "\\uploads\\uploaded-files\\" + uploadedFile.getName());

        InputStreamResource resource = new InputStreamResource(new FileInputStream(requestedFile));
        HttpHeaders headers = new HttpHeaders();

        headers.add("Content-Disposition", "attachment; filename=" + requestedFile.getName());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok().headers(headers).contentLength(requestedFile.length())
                .contentType(new MediaType(MediaType.parseMediaType("application/pdf"))).body(resource);
    }

    @PostMapping("/topic-repository/delete-file")
    public String processUploadedFileRequest(@RequestParam("fileId") Long fileId, RedirectAttributes redirectAttributes) {
        User loggedInUserInstance = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.loadUserByUsername(loggedInUserInstance.getEmail());

        Long topicId = uploadedFileService.getUploadedFileById(fileId).getTopic().getId();
        redirectAttributes.addAttribute("topicId", topicId);

        String fileName = uploadedFileService.getUploadedFileById(fileId).getName();
        uploadedFileService.deleteFile("file", fileName);
        uploadedFileService.deleteUploadedFileById(fileId);

        if (currentUser.getRole().equals("PROFESSOR_ROLE"))
            return "redirect:/api/professor/" + currentUser.getId() + "/topic-repository";

        return "redirect:/api/student/" + currentUser.getId() + "/topic-repository";
    }

}
