package com.petros.diplomate.service;

import com.petros.diplomate.model.Topic;
import com.petros.diplomate.model.UploadedFile;
import com.petros.diplomate.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UploadedFileService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private EmailService emailService;


    public UploadedFile getUploadedFileById(Long fileId) {
        return uploadedFileRepository.findById(fileId).orElseThrow(
                () -> new IllegalStateException("Uploaded file with id " + fileId + " not found")
        );
    }

    public Optional<UploadedFile> getUploadedFileByName(String fileName) {
        return uploadedFileRepository.findByName(fileName);
    }

    public void deleteUploadedFileById(Long fileId) {
        uploadedFileRepository.deleteById(fileId);
    }

    public void saveNewFile(String name, String uploader, LocalDate uploadedDate, Topic topic) throws IllegalStateException {
        Optional<UploadedFile> existedFile = this.getUploadedFileByName(name);

        if (existedFile.isPresent())
            throw new IllegalStateException("A file with this name already exists, please rename your file, and try again");

        UploadedFile file = new UploadedFile(name, uploader, uploadedDate, topic);
        uploadedFileRepository.save(file);

        String uploaderName = "";
        String receiverName = "";
        String receiverEmail = "";

        switch (uploader) {
            case "STUDENT_UPLOADER": {
                uploaderName = topic.getStudent().getFullName();
                receiverName = topic.getProfessor().getFirstName();
                receiverEmail = topic.getProfessor().getEmail();
                break;
            }
            case "PROFESSOR_UPLOADER": {
                uploaderName = topic.getProfessor().getFullName();
                receiverName = topic.getStudent().getFirstName();
                receiverEmail = topic.getStudent().getEmail();
                break;
            }
        }

        emailService.send(receiverEmail, emailService.buildFileUploadedEmail(receiverName, uploaderName, topic.getName()),
                   "New File Upload");

    }

    public List<UploadedFile> getUploadedFilesByUploaderAndTopic(String uploader, Topic topic) {
        List<UploadedFile> uploadedFilesByUploader = uploadedFileRepository.findAllByUploader(uploader);
        List<UploadedFile> uploadedFilesByTopic = uploadedFileRepository.findAllByTopic(topic);

        // keep only the uploaded files that are the same in both list inside the uploadedFilesByUploader list
        uploadedFilesByUploader.retainAll(uploadedFilesByTopic);

        return uploadedFilesByUploader;
    }

    public void uploadFile(MultipartFile file, String fileType, String fileName) throws IOException {
        Path currentRelativePath = Paths.get("");
        String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();

        switch (fileType) {
            case "image":
                file.transferTo(new File(currentAbsolutePath + "\\uploads\\profile-images\\" + fileName));
                break;
            case "gradesPDF":
                file.transferTo(new File(currentAbsolutePath + "\\uploads\\grades-pdfs\\" + fileName));
                break;
            case "file":
                file.transferTo(new File(currentAbsolutePath + "\\uploads\\uploaded-files\\" + fileName));
                break;
        }
    }

    public void deleteFile(String fileType, String fileName) {
        Path currentRelativePath = Paths.get("");
        String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();

        switch (fileType) {
            case "image": {
                File file = new File(currentAbsolutePath + "\\uploads\\profile-images\\" + fileName);
                file.delete();
                break;
            }
            case "gradesPDF": {
                File file = new File(currentAbsolutePath + "\\uploads\\grades-pdfs\\" + fileName);
                file.delete();
                break;
            }
            case "file": {
                File file = new File(currentAbsolutePath + "\\uploads\\uploaded-files\\" + fileName);
                file.delete();
                break;
            }
        }
    }

}
