package com.petros.diplomate.service;

import com.petros.diplomate.model.Course;
import com.petros.diplomate.model.RequiredCourse;
import com.petros.diplomate.model.Topic;
import com.petros.diplomate.repository.RequiredCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RequiredCourseService {

    @Autowired
    private RequiredCourseRepository requiredCourseRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TopicService topicService;


    public void initializeRequiredCourses(Topic topic, List<Course> courses) {
        for (Course course : courses)
            topic.getRequiredCourses().add(new RequiredCourse(courseService.getCourseByName(course.getName()), 0.0, topic));
    }

    public void addRequiredCourses(Topic newTopic, List<RequiredCourse> requiredCourses) {
        List<Course> courses = courseService.getAllCourses();

        for (int i=0; i<requiredCourses.size(); i++) {
            RequiredCourse newRequiredCourse = new RequiredCourse(courses.get(i), requiredCourses.get(i).getMinScore(), newTopic);
            requiredCourseRepository.save(newRequiredCourse);
        }

        topicService.saveTopic(newTopic);
    }

    public void updateRequiredCoursesIfNeeded(Topic currentTopic) {
        List<RequiredCourse> currentRequiredCourses = currentTopic.getRequiredCourses();

        List<String> currentCourses = new ArrayList<>();

        for (RequiredCourse requiredCourse : currentRequiredCourses)
            currentCourses.add(requiredCourse.getCourse().getName());

        for (Course course : courseService.getAllCourses())
            if (!currentCourses.contains(course.getName())) {
                RequiredCourse newRequiredCourse = new RequiredCourse(course, 0.0, currentTopic);
                currentTopic.getRequiredCourses().add(newRequiredCourse);

                requiredCourseRepository.save(newRequiredCourse);
            }

        topicService.saveTopic(currentTopic);
    }

}
