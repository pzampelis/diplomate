package com.petros.diplomate.service;

import com.petros.diplomate.model.*;
import com.petros.diplomate.repository.SelectedTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SelectedTagService {

    @Autowired
    private SelectedTagRepository selectedTagRepository;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserService userService;


    public List<SelectedTag> getSelectedTagsByTagId(Long tagId) {
        return selectedTagRepository.findAllByTagId(tagId);
    }

    public List<SelectedTag> getAllSelectedTopicTags() {
        return selectedTagRepository.findAllByType("TOPIC_TYPE");
    }

    public List<String> getSelectedTopicTagsAsStringListByTopic(Topic topic) {
        List<String> selectedTagsStringList = new ArrayList<>();

        for (SelectedTopicTag tag : this.getSelectedTopicTagsByTopic(topic))
            if (tag.isSelected())
                selectedTagsStringList.add(tag.getTag().getText());

        return selectedTagsStringList;
    }

    public List<SelectedTopicTag> getSelectedTopicTagsByTopic(Topic topic) {
        List<SelectedTopicTag> topicSelectedTopicTags = new ArrayList<>();

        for (SelectedTag tag : this.getAllSelectedTopicTags())
            if (((SelectedTopicTag) tag).getTopic().equals(topic))
                topicSelectedTopicTags.add((SelectedTopicTag) tag);

        return topicSelectedTopicTags;
    }

    public void deleteSelectedTag(SelectedTag selectedTag) {
        selectedTagRepository.delete(selectedTag);
    }

    public void addSelectedTagsByTopic(Topic newTopic, List<String> selectedTags, List<Tag> allTags) {
        for (Tag tag : allTags) {
            SelectedTopicTag newSelectedTopicTag;

            if (selectedTags.contains(tag.getText()))
                newSelectedTopicTag = new SelectedTopicTag(true, tag, newTopic);
            else
                newSelectedTopicTag = new SelectedTopicTag(false, tag, newTopic);

            selectedTagRepository.save(newSelectedTopicTag);
        }

        topicService.saveTopic(newTopic);
    }

    public void updateSelectedTopicTagsIfNeeded(Topic topic, List<Tag> allTags) {
        List<SelectedTopicTag> currentSelectedTopicTags = topic.getSelectedTopicTags();

        List<String> currentTags = new ArrayList<>();

        for (SelectedTopicTag selectedTopicTag : currentSelectedTopicTags)
            currentTags.add(selectedTopicTag.getTag().getText());

        for (Tag tag : allTags)
            if (!currentTags.contains(tag.getText())) {
                SelectedTopicTag newSelectedTopicTag = new SelectedTopicTag(false, tag, topic);
                topic.getSelectedTopicTags().add(newSelectedTopicTag);
                selectedTagRepository.save(newSelectedTopicTag);
            }

        topicService.saveTopic(topic);
    }

    public void updateSelectedStudentTagsIfNeeded(Student student, List<Tag> allTags) {
        List<SelectedStudentTag> currentSelectedStudentTags = student.getSelectedStudentTags();

        List<String> currentTags = new ArrayList<>();

        for (SelectedStudentTag selectedStudentTag : currentSelectedStudentTags)
            currentTags.add(selectedStudentTag.getTag().getText());

        for (Tag tag : allTags) {
            if (!currentTags.contains(tag.getText())) {
                SelectedStudentTag newSelectedStudentTag =
                        new SelectedStudentTag(false, tag, student);
                student.getSelectedStudentTags().add(newSelectedStudentTag);
                selectedTagRepository.save(newSelectedStudentTag);
            }
        }

        userService.saveUser(student);
    }

    public String getSelectedStudentTagsAsString(Student student) {
        StringBuilder selectedStudentTagsString = new StringBuilder();

        for (SelectedStudentTag selectedStudentTag : student.getSelectedStudentTags())
            if (selectedStudentTag.isSelected() && selectedStudentTagsString.length() == 0)
                selectedStudentTagsString.append(selectedStudentTag.getTag().getText());
            else if (selectedStudentTag.isSelected())
                selectedStudentTagsString.append(", ").append(selectedStudentTag.getTag().getText());

        return selectedStudentTagsString.toString();
    }

    public List<String> getSelectedStudentTagsAsStringsList(Student student) {
        List<String> selectedTagsStringList = new ArrayList<>();

        for (SelectedStudentTag tag : student.getSelectedStudentTags())
            if (tag.isSelected())
                selectedTagsStringList.add(tag.getTag().getText());

        return selectedTagsStringList;
    }

    public String getSelectedTopicTagsAsString(Topic topic) {
        StringBuilder selectedTopicTagsString = new StringBuilder();

        for (SelectedTopicTag selectedTopicTag : topic.getSelectedTopicTags())
            if (selectedTopicTag.isSelected() && selectedTopicTagsString.length() == 0)
                selectedTopicTagsString.append(selectedTopicTag.getTag().getText());
            else if (selectedTopicTag.isSelected())
                selectedTopicTagsString.append(", ").append(selectedTopicTag.getTag().getText());

        return selectedTopicTagsString.toString();
    }

    public List<String> getSelectedTopicTagsAsStringsList(Topic topic) {
        List<String> selectedTagsStringList = new ArrayList<>();

        for (SelectedTopicTag tag : topic.getSelectedTopicTags())
            if (tag.isSelected())
                selectedTagsStringList.add(tag.getTag().getText());

        return selectedTagsStringList;
    }

    public void checkSelectedTagsValidity(List<String> selectedTags) throws IllegalStateException {
        if (selectedTags == null || selectedTags.size() > 5)
            throw new IllegalStateException("You must select at least one(1), and not more than five(5) tags");
    }

}
