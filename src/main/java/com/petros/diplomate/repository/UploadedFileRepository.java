package com.petros.diplomate.repository;

import com.petros.diplomate.model.Topic;
import com.petros.diplomate.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedFileRepository  extends JpaRepository<UploadedFile, Long> {

    Optional<UploadedFile> findByName(String name);

    List<UploadedFile> findAllByTopic(Topic topic);

    List<UploadedFile> findAllByUploader(String uploader);

}
