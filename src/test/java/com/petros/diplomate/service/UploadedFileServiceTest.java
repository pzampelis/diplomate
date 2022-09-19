package com.petros.diplomate.service;

import com.petros.diplomate.model.Professor;
import com.petros.diplomate.model.Student;
import com.petros.diplomate.model.Topic;
import com.petros.diplomate.model.UploadedFile;
import com.petros.diplomate.repository.UploadedFileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class UploadedFileServiceTest {

    @MockBean
    private MultipartFile multipartFile;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private UploadedFileService uploadedFileService;


    @Test
    void givingANewName_whenSavingANewFile_shouldCallSaveOneTime() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor, "STANDARD_TYPE");
        topic.setStudent(student);

        uploadedFileService.saveNewFile("aName", "PROFESSOR_UPLOADER", LocalDate.now(), topic);

        Mockito.verify(uploadedFileRepository, Mockito.times(1)).save(Mockito.any(UploadedFile.class));
    }

    @Test
    void givingAnExistingName_whenSavingANewFile_shouldCallSaveOneTime() {
        Professor professor = new Professor("pfn", "pln", "pe", "pp", "PROFESSOR_ROLE");
        Student student = new Student("sfn", "sln", "se", "sp", "STUDENT_ROLE");
        Topic topic = new Topic("tn", "td", LocalDate.now().plusDays(1), professor, "STANDARD_TYPE");
        topic.setStudent(student);
        UploadedFile uploadedFile = new UploadedFile("aName", "PROFESSOR_UPLOADER", LocalDate.now(), topic);

        Mockito.when(uploadedFileService.getUploadedFileByName("aName")).thenReturn(java.util.Optional.of(uploadedFile));
        IllegalStateException exception = null;
        try {
            uploadedFileService.saveNewFile("aName", "PROFESSOR_UPLOADER", LocalDate.now(), topic);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("A file with this name already exists, please rename your file, and try again", exception.getMessage());
    }

    @Test
    void uploadFile_shouldCallTransferToOneTime() throws IOException {
        uploadedFileService.uploadFile(multipartFile, "image", "aName");

        Mockito.verify(multipartFile, Mockito.times(1)).transferTo(Mockito.any(File.class));
    }

}
