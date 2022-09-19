package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class UploadedFile {

    @Id
    @SequenceGenerator(name = "uploaded_file_sequence", sequenceName = "uploaded_file_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uploaded_file_sequence")
    private Long id;

    @Column(unique = true)
    private String name;

    private String uploader;        // PROFESSOR_UPLOADER, STUDENT_UPLOADER

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "topic_id")
    private Topic topic;


    public UploadedFile(String name, String uploader, LocalDate uploadDate, Topic topic) {
        this.name = name;
        this.uploader = uploader;
        this.uploadDate = uploadDate;
        this.topic = topic;
    }

}
