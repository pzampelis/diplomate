package com.petros.diplomate.service;

import com.petros.diplomate.model.Course;
import com.petros.diplomate.repository.CourseRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @MockBean
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;


    @Test
    public void addingACourse_whenUpdatingAnEmptyCoursesList_shouldCallSaveOneTime() {
        courseService.updateCoursesList("aCourse");

        Mockito.verify(courseRepository).save(Mockito.any(Course.class));
    }

    @Test
    public void addingTwoCourse_whenUpdatingAnEmptyCoursesList_shouldCallSaveTwoTimes() {
        courseService.updateCoursesList("aCourse\r\nanotherCourse");

        Mockito.verify(courseRepository, Mockito.times(2)).save(Mockito.any(Course.class));
    }

    @Test
    public void checkingCoursesListValidity_whenGivenCoursesListIsValid_shouldNotReturnException() {
        List<String> validCoursesList = new ArrayList<>();
        validCoursesList.add("aCourse");
        validCoursesList.add("anotherCourse");

        IllegalStateException exception = null;
        try {
            courseService.checkCoursesListValidity(validCoursesList);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertNull(exception);
    }

    @Test
    public void checkingCoursesListValidity_whenGivenCoursesListIsNotValid_shouldReturnException() {
        List<String> validCoursesList = new ArrayList<>();
        validCoursesList.add("aCourse");
        validCoursesList.add("aCourse");

        IllegalStateException exception = null;
        try {
            courseService.checkCoursesListValidity(validCoursesList);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("Invalid courses list (contains duplicate courses)", exception.getMessage());
    }

}
