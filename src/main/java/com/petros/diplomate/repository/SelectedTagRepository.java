package com.petros.diplomate.repository;

import com.petros.diplomate.model.SelectedTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectedTagRepository extends JpaRepository<SelectedTag, Long> {

    List<SelectedTag> findAllByTagId(Long tagId);

    List<SelectedTag> findAllByType(String type);

}
