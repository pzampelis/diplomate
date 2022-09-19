package com.petros.diplomate.service;

import com.petros.diplomate.model.Course;
import com.petros.diplomate.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;


    public Course getCourseByName(String courseName) throws IllegalStateException {
        return courseRepository.findByName(courseName).orElseThrow(
                () -> new IllegalStateException("Course with name " + courseName + " not found")
        );
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public String getCoursesAsString() {
        List<Course> coursesList = this.getAllCourses();
        StringBuilder coursesString = new StringBuilder();

        for (Course course : coursesList) {
            coursesString.append(course.getName()).append("\n");
        }

        return coursesString.toString();
    }

    public void updateCoursesList(String updatedCourses) {
        List<String> oldCourses = new ArrayList<>();
        Collections.addAll(oldCourses, this.getCoursesAsString().split("\n"));
        oldCourses.removeIf(String::isBlank);

        List<String> newCourses = new ArrayList<>();
        Collections.addAll(newCourses, updatedCourses.split("\r\n"));
        newCourses.removeIf(String::isBlank);

        this.checkCoursesListValidity(newCourses);

        List<String> oldCoursesCopy = new ArrayList<>(oldCourses);
        List<String> newCoursesCopy = new ArrayList<>(newCourses);

        // added courses (are stored at newCoursesCopy)
        newCoursesCopy.removeAll(oldCoursesCopy);

        for (String addedCourseName : newCoursesCopy) {
            Course addedCourse = new Course(addedCourseName);
            courseRepository.save(addedCourse);
        }

        // removed courses (are stored at oldCourses)
        oldCourses.removeAll(newCourses);

        for (String removedCourseName : oldCourses) {
            Course removedCourse = this.getCourseByName(removedCourseName);
            courseRepository.deleteById(removedCourse.getId());
        }
    }

    public void checkCoursesListValidity(List<String> coursesList) throws IllegalStateException {
        if (coursesList.size() > 100)
            throw new IllegalStateException("Invalid courses list (contains more than 100 courses)");

        Set<String> coursesSet = new HashSet<>(coursesList);

        if (coursesSet.size() < coursesList.size())     // check for duplicates
            throw new IllegalStateException("Invalid courses list (contains duplicate courses)");
    }

}
