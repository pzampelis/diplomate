package com.petros.diplomate.service;

import com.petros.diplomate.model.*;
import com.petros.diplomate.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private TagService tagService;

    @Autowired
    private SelectedTagService selectedTagService;


    public Topic getTopicById(Long topicId) throws IllegalStateException {
        return topicRepository.findById(topicId).orElseThrow(
                () -> new IllegalStateException("Topic with id " + topicId + " not found")
        );
    }

    public Optional<Topic> getTopicByStudentId(Long studentId) throws IllegalStateException {
        return topicRepository.findByStudentId(studentId);
    }

    public Optional<Topic> getTopicByName(String topicName) {
        return topicRepository.findByName(topicName);
    }

    public void saveTopic(Topic topic) {
        topicRepository.save(topic);
    }

    public void deleteTopicById(Long topicId) {
        Topic topic = this.getTopicById(topicId);

        for (Student candidate : topic.getCandidates())
            emailService.send(candidate.getEmail(), emailService.buildTopicDeletedEmail(candidate.getFirstName(), topic.getName()),
                              "Topic Deleted");

        if (topic.getStudent() != null)
            emailService.send(topic.getStudent().getEmail(),
                    emailService.buildTopicDeletedEmail(topic.getStudent().getFirstName(), topic.getName()),
                    "Topic Deleted");

        topicRepository.deleteById(topicId);
    }

    public List<Topic> getAvailableStandardTopics() {
        List<Topic> availableTopics = new ArrayList<>();

        for (Topic topic : topicRepository.findAll())
            if (topic.getStudent() == null && topic.getApplicationDeadline().isAfter(LocalDate.now()) &&
                    topic.getType().equals("STANDARD_TYPE"))
                availableTopics.add(topic);

        return availableTopics;
    }

    public List<Topic> getUnassignedStandardTopics() {
        List<Topic> unassignedTopics = new ArrayList<>();

        for (Topic topic : topicRepository.findAll())
            if (topic.getStudent() == null && topic.getType().equals("STANDARD_TYPE"))
                unassignedTopics.add(topic);

        return unassignedTopics;
    }

    public Topic createTopic(Topic topic, String topicType, Professor professor, List<String> selectedTags) {
        this.isTopicNameTaken(topic.getName());
        selectedTagService.checkSelectedTagsValidity(selectedTags);

        Topic newTopic = new Topic(topic.getName(), topic.getDescription(), topic.getApplicationDeadline(), professor,
                                   topicType);
        topicRepository.save(newTopic);

        List<Tag> allTags = tagService.getAllTags();
        selectedTagService.addSelectedTagsByTopic(newTopic, selectedTags, allTags);

        return newTopic;
    }

    public boolean isTopicNameTaken(String topicName) throws IllegalStateException {
        if (this.getTopicByName(topicName).isPresent())
            throw new IllegalStateException("This name is already taken");

        return false;
    }

    public void updateTopic(Topic updatedTopic, List<String> updatedSelectedTags) {
        Topic existedTopic = this.getTopicById(updatedTopic.getId());

        if ((!((existedTopic.getName()).equals(updatedTopic.getName())) && !this.isTopicNameTaken(updatedTopic.getName())) ||
            !((existedTopic.getDescription()).equals(updatedTopic.getDescription())) ||
            !((existedTopic.getApplicationDeadline()).equals(updatedTopic.getApplicationDeadline())) ||
            !((existedTopic.getRequiredCourses()).equals(updatedTopic.getRequiredCourses()))) {

            existedTopic.setName(updatedTopic.getName());
            existedTopic.setDescription(updatedTopic.getDescription());
            existedTopic.setApplicationDeadline(updatedTopic.getApplicationDeadline());

            selectedTagService.checkSelectedTagsValidity(updatedSelectedTags);

            for (SelectedTopicTag selectedTopicTag : existedTopic.getSelectedTopicTags())
                if (updatedSelectedTags.contains(selectedTopicTag.getTag().getText()))
                    selectedTopicTag.setSelected(true);
                else
                    selectedTopicTag.setSelected(false);

            if (updatedTopic.getType().equals("STANDARD_TYPE"))
                for (int i = 0; i < existedTopic.getRequiredCourses().size(); i++)
                    existedTopic.getRequiredCourses().get(i).setMinScore(updatedTopic.getRequiredCourses().get(i).getMinScore());
        }

        topicRepository.save(existedTopic);
    }

    public List<Topic> getTopicsBySearch(Professor professor, List<String> selectedTags) throws IllegalStateException {
        if (selectedTags.size() > 5)
            throw new IllegalStateException("You can not select more than five(5) tags");
        else if (professor == null && selectedTags.isEmpty())
            return this.getAvailableStandardTopics();
        else {
            List<Topic> matchingTopicsByProfessorFullName = new ArrayList<>();

            if (professor != null)
                matchingTopicsByProfessorFullName.addAll(this.getAvailableStandardTopicsByProfessorId(professor.getId()));
            else
                matchingTopicsByProfessorFullName.addAll(this.getAvailableStandardTopics());

            if (selectedTags.isEmpty())
                return matchingTopicsByProfessorFullName;

            Map<Topic, Double> matchingTopicsMap = new HashMap<>();

            for (Topic topic : matchingTopicsByProfessorFullName) {
                List<String> tagsInTopic = selectedTagService.getSelectedTopicTagsAsStringsList(topic);

                double similarity = this.calculateSimilarity(selectedTags, tagsInTopic);

                if (similarity > 0)
                    matchingTopicsMap.put(topic, similarity);
            }

            return this.getOrderedTopicsListFromMap(matchingTopicsMap);
        }
    }

    public double calculateSimilarity(List<String> studentTags, List<String> topicTags) {
        if (studentTags.size() == 0 || topicTags.size() == 0)
            return 0;

        int commonItems = 0;
        double similarity;

        for (String s : studentTags)
            if (topicTags.contains(s))
                commonItems++;

        similarity = (double) commonItems / (studentTags.size() + topicTags.size() - commonItems);  // jaccard similarity

        return similarity;
    }

    private List<Topic> getAvailableStandardTopicsByProfessorId(Long professorId) {
        List<Topic> matchingTopics = new ArrayList<>();

        for (Topic topic : this.getAvailableStandardTopics())
            if (topic.getProfessor().getId().equals(professorId))
                matchingTopics.add(topic);

        return matchingTopics;
    }

    public void addCandidate(Topic topic, Student student) throws IllegalStateException {
        this.checkGradesPDFAvailability(student.getGradesPDFPath());
        if (topic.getCandidates().contains(student))
            throw new IllegalStateException("You have already applied for this topic");

        topic.getCandidates().add(student);
        topicRepository.save(topic);
    }

    public void removeCandidate(Topic topic, Student student) {
        topic.getCandidates().remove(student);
        topicRepository.save(topic);
    }

    public void acceptCandidate(Topic topic, Student student) {
        Optional<Topic> studentTopic = this.getTopicByStudentId(student.getId());
        if (studentTopic.isPresent())
            this.formatRejectedOpenTopic(studentTopic.get(), student);

        topic.setStudent(student);
        String emailSubject = "Accepted for Topic";
        emailService.send(student.getEmail(), emailService.buildTopicUpdateEmail(student.getFirstName(), emailSubject, topic.getName()),
                          emailSubject);

        topic.setStatus("Topic given");

        this.removeAndInformRemainingCandidates(topic);
        this.removeStudentFromOtherTopics(student);
    }

    public void removeAndInformRemainingCandidates(Topic topic) {
        for (Student student : topic.getCandidates())
            if (topic.getStudent() != student) {
                String emailSubject = "Rejected for Topic";
                emailService.send(student.getEmail(), emailService.buildTopicUpdateEmail(student.getFirstName(), emailSubject,
                        topic.getName()), emailSubject);
            }

        topic.getCandidates().clear();
        topicRepository.save(topic);
    }

    public void removeStudentFromOtherTopics(Student student) {
        for (Topic topic : this.getUnassignedStandardTopics())
            if (topic.getCandidates().contains(student)) {
                InterviewSession arrangedInvitation = sessionService.getInterviewSessionByProfessorAndStudent(
                                                            topic.getProfessor(), student);
                if (arrangedInvitation != null) {
                    emailService.send(topic.getProfessor().getEmail(), emailService.buildCancelInterviewByStudentEmail(arrangedInvitation),
                               "Interview Canceled");
                    sessionService.deleteSessionById(arrangedInvitation.getId());
                }

                topic.getCandidates().remove(student);
                topicRepository.save(topic);
            }
    }

    public List<Topic> getOldTopicsByProfessorId(Long professorId) {
        List<Topic> oldProfessorTopics = new ArrayList<>();

        for (Topic topic : topicRepository.findAllByProfessorId(professorId))
            if (!topic.isActive())
                oldProfessorTopics.add(topic);

        return oldProfessorTopics;
    }

    public List<Topic> getActiveTopicsByProfessorId(Long professorId) {
        List<Topic> activeProfessorTopics = new ArrayList<>();

        for (Topic topic : topicRepository.findAllByProfessorId(professorId))
            if (topic.isActive())
                activeProfessorTopics.add(topic);

        return activeProfessorTopics;
    }

    public List<Topic> getAvailableActiveStandardTopicsByProfessor(Professor professor) {
        List<Topic> matchingTopics = new ArrayList<>();

        for (Topic topic : this.getActiveTopicsByProfessorId(professor.getId()))
            if (topic.getStudent() == null && topic.getType().equals("STANDARD_TYPE"))
                matchingTopics.add(topic);

        return matchingTopics;
    }

    public List<Topic> getAvailableActiveOpenTopicsByProfessor(Professor professor) {
        List<Topic> matchingTopics = new ArrayList<>();

        for (Topic topic : this.getActiveTopicsByProfessorId(professor.getId()))
            if (topic.getType().equals("OPEN_TYPE"))
                if (topic.getStudent() == null || (topic.getStudent() != null && topic.getAcceptanceDeadline() != null))
                    matchingTopics.add(topic);

        return matchingTopics;
    }

    public List<Topic> getTakenActiveTopicsByProfessorId(Long professorId) {
        List<Topic> matchingTopics = new ArrayList<>();

        for (Topic topic : topicRepository.findAllByProfessorId(professorId))
            if (topic.getStudent() != null && topic.getAcceptanceDeadline() == null  && topic.isActive())
                matchingTopics.add(topic);

        return matchingTopics;
    }

    public void updateTopicsStatus(Student student) {
        for (Topic topic : this.getUnassignedStandardTopics())
            if (topic.getCandidates().contains(student) && topic.getApplicationDeadline().isBefore(LocalDate.now()))
                topic.setStatus("Interviews period");
    }

    public boolean hasTopicRequiredCourses(Topic topic) {
        for (RequiredCourse reqCourse : topic.getRequiredCourses())
            if (reqCourse.getMinScore() >= 5.0)
                return true;

        return false;
    }

    public List<Student> getStudentsByProfessor(Professor professor) {
        List<Student> students = new ArrayList<>();

        for (Topic topic : this.getActiveTopicsByProfessorId(professor.getId()))
            if (topic.getStudent() != null)
                students.add(topic.getStudent());

        return students;
    }

    public List<Student> getUninvitedCandidates(Topic topic) {
        List<Student> uninvitedStudents = new ArrayList<>();

        for (Student student : topic.getCandidates())
            if (sessionService.getInterviewSessionByProfessorAndStudent(topic.getProfessor(), student) == null)
                uninvitedStudents.add(student);

        return uninvitedStudents;
    }

    public void notifyPossiblyInterestedStudents(String topicName, List<Student> studentsWithoutTopic) {
        Topic topic = this.getTopicByName(topicName).get();
        List<String> selectedTopicTags = selectedTagService.getSelectedTopicTagsAsStringListByTopic(topic);

        for (Student student : studentsWithoutTopic) {
            List<String> selectedStudentTags = selectedTagService.getSelectedStudentTagsAsStringsList(student);

            double similarity = this.calculateSimilarity(selectedStudentTags, selectedTopicTags);

            if (similarity >= 0.6)
                emailService.send(student.getEmail(), emailService.buildNewTopicNotificationEmail(student.getFirstName(), topic),
                           "New Topic Added");
        }
    }

    public Topic offerOpenTopic(Student student) throws IllegalStateException {
        Optional<Topic> studentTopic = this.getTopicByStudentId(student.getId());
        if (studentTopic.isPresent())
            if (studentTopic.get().getAcceptanceDeadline() != null)
                throw new IllegalStateException("We have already offered you a topic, reject it and then try again if you want a new one");

        List<Topic> availableOpenTopics = this.getAvailableOpenTopics();
        List<String> selectedStudentTags = selectedTagService.getSelectedStudentTagsAsStringsList(student);

        Map<Topic, Double> matchingTopicsMap = new HashMap<>();

        for (Topic topic : availableOpenTopics) {
            List<String> tagsInTopic = selectedTagService.getSelectedTopicTagsAsStringsList(topic);
            double similarity = this.calculateSimilarity(selectedStudentTags, tagsInTopic);
            matchingTopicsMap.put(topic, similarity);
        }

        List<Topic> orderedAvailableOpenTopics = this.getOrderedTopicsListFromMap(matchingTopicsMap);

        for (Topic topic : orderedAvailableOpenTopics)
            if (!topic.getNotInterestedStudents().contains(student)) {
                topic.setStudent(student);
                topic.setAcceptanceDeadline(LocalDateTime.now().plusMinutes(1440));     // one day from now
                topicRepository.save(topic);
                return topic;
            }

        throw new IllegalStateException("Sorry, there is no available topic to offer you");
    }

    private List<Topic> getAvailableOpenTopics() {
        List<Topic> availableOpenTopics = new ArrayList<>();

        for (Topic topic : topicRepository.findAll())
            if (topic.getType().equals("OPEN_TYPE") && topic.getApplicationDeadline().isAfter(LocalDate.now()))
                if (topic.getStudent() == null && topic.getAcceptanceDeadline() == null)
                    availableOpenTopics.add(topic);
                else if (topic.getStudent() != null && topic.getAcceptanceDeadline() != null)
                    if (topic.getAcceptanceDeadline().isBefore(LocalDateTime.now())) {
                        topic.setStudent(null);
                        topic.setAcceptanceDeadline(null);
                        topicRepository.save(topic);
                        availableOpenTopics.add(topic);
                    }

        return availableOpenTopics;
    }

    public List<Topic> getOrderedTopicsListFromMap(Map<Topic, Double> topicsMap) {
        Map<Topic, Double> sortedMatchingTopicsMap = topicsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        List<Topic> orderedTopics = new ArrayList<>();

        for (int i=sortedMatchingTopicsMap.size()-1; i>=0; i--)
            orderedTopics.add((Topic) (sortedMatchingTopicsMap.keySet().toArray())[i]);

        return orderedTopics;
    }


    public boolean isOfferedTopicAvailable(Student student) {
        Optional<Topic> studentTopic = this.getTopicByStudentId(student.getId());
        if (studentTopic.isPresent() && studentTopic.get().getAcceptanceDeadline() != null &&
                studentTopic.get().getAcceptanceDeadline().isAfter(LocalDateTime.now()))
            return true;

        if (studentTopic.isPresent() && studentTopic.get().getAcceptanceDeadline() != null) {
            Topic topic = studentTopic.get();
            formatRejectedOpenTopic(topic, student);
        }

        return false;
    }

    public void acceptOpenTopic(Student student) {
        Topic topic = this.getTopicByStudentId(student.getId()).get();
        topic.setAcceptanceDeadline(null);
        topicRepository.save(topic);
        this.removeStudentFromOtherTopics(student);

        emailService.send(topic.getProfessor().getEmail(), emailService.buildNotifyProfessorAboutOpenTopicWasTakenEmail(topic),
                          "Topic Taken Over");
    }

    public void rejectOpenTopic(Student student) {
        Topic topic = this.getTopicByStudentId(student.getId()).get();
        formatRejectedOpenTopic(topic, student);
    }

    public void formatRejectedOpenTopic(Topic topic, Student student) {
        topic.getNotInterestedStudents().add(student);
        topic.setAcceptanceDeadline(null);
        topic.setStudent(null);
        topicRepository.save(topic);
    }

    public void checkGradesPDFAvailability(String gradesPDFPath) throws IllegalStateException {
        if (gradesPDFPath.equals(""))
            throw new IllegalStateException("You must upload your grades PDF before you can continue");
    }

    public List<Topic> getTopicsStudentIsInterestedIn(Student student) {
        List<Topic> matchingTopics = new ArrayList<>();

        for (Topic topic : this.getUnassignedStandardTopics())
            if (topic.getCandidates().contains(student))
                matchingTopics.add(topic);

        return matchingTopics;
    }

}
