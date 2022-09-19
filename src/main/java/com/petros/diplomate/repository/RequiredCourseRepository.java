package com.petros.diplomate.repository;

import com.petros.diplomate.model.RequiredCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequiredCourseRepository extends JpaRepository<RequiredCourse, Long> {

}
