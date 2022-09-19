package com.petros.diplomate.service;

import com.petros.diplomate.model.SelectedTag;
import com.petros.diplomate.model.Tag;
import com.petros.diplomate.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SelectedTagService selectedTagService;


    public Tag getTagByText(String tagText) throws IllegalStateException {
        return tagRepository.findByText(tagText).orElseThrow(
                () -> new IllegalStateException("Tag with text " + tagText + " not found")
        );
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<String> getTagsAsStringsList() {
        List<String> tags = new ArrayList<>();

        for (Tag tag : this.getAllTags())
            tags.add(tag.getText());

        return tags;
    }

    public String getTagsAsString() {
        List<Tag> tagsList = this.getAllTags();
        StringBuilder tagsString = new StringBuilder();

        for (Tag tag : tagsList) {
            tagsString.append(tag.getText()).append("\n");
        }

        return tagsString.toString();
    }

    public void updateTagsList(String updatedTags) {
        List<String> oldTags = new ArrayList<>();
        Collections.addAll(oldTags, this.getTagsAsString().split("\n"));
        oldTags.removeIf(String::isBlank);

        List<String> newTags = new ArrayList<>();
        Collections.addAll(newTags, updatedTags.split("\r\n"));
        newTags.removeIf(String::isBlank);

        this.checkTagsListValidity(newTags);

        List<String> oldTagsCopy = new ArrayList<>(oldTags);
        List<String> newTagsCopy = new ArrayList<>(newTags);

        // added tags (are stored at newTagsCopy)
        newTagsCopy.removeAll(oldTagsCopy);

        for (String addedTagText : newTagsCopy) {
            Tag addedTag = new Tag(addedTagText);
            tagRepository.save(addedTag);
        }

        // removed tags (are stored at oldTags)
        oldTags.removeAll(newTags);

        for (String removedTagText : oldTags) {
            Tag removedTag = this.getTagByText(removedTagText);

            if (isTagNotInUse(removedTag))
                tagRepository.deleteById(removedTag.getId());
        }
    }

    public void checkTagsListValidity(List<String> tagsList) throws IllegalStateException {
        if (tagsList.size() > 50)
            throw new IllegalStateException("Invalid tags list (contains more than 50 tags)");

        Set<String> tagsSet = new HashSet<>(tagsList);

        if (tagsSet.size() < tagsList.size())     // check for duplicates
            throw new IllegalStateException("Invalid tags list (contains duplicate tags)");
    }

    public boolean isTagNotInUse(Tag tag) {
        List<SelectedTag> selectedTags = selectedTagService.getSelectedTagsByTagId(tag.getId());

        for (SelectedTag selectedTag : selectedTags)
            if (selectedTag.isSelected())
                return false;

        for (SelectedTag selectedTag : selectedTags)
            selectedTagService.deleteSelectedTag(selectedTag);

        return true;
    }

}
