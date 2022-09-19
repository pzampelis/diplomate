package com.petros.diplomate.service;

import com.petros.diplomate.model.Tag;
import com.petros.diplomate.repository.TagRepository;
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
class TagServiceTest {

    @MockBean
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;


    @Test
    public void addingATag_whenUpdatingAnEmptyTagsList_shouldCallSaveOneTime() {
        tagService.updateTagsList("aTag");

        Mockito.verify(tagRepository).save(Mockito.any(Tag.class));
    }

    @Test
    public void addingTwoTags_whenUpdatingAnEmptyTagsList_shouldCallSaveTwoTimes() {
        tagService.updateTagsList("aTag\r\nanotherTag");

        Mockito.verify(tagRepository, Mockito.times(2)).save(Mockito.any(Tag.class));
    }

    @Test
    public void checkingTagsListValidity_whenGivenTagsListIsValid_shouldNotReturnException() {
        List<String> validTagsList = new ArrayList<>();
        validTagsList.add("aTag");
        validTagsList.add("anotherTag");

        IllegalStateException exception = null;
        try {
            tagService.checkTagsListValidity(validTagsList);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertNull(exception);
    }

    @Test
    public void checkingTagsListValidity_whenGivenTagsListIsNotValid_shouldReturnException() {
        List<String> validTagsList = new ArrayList<>();
        validTagsList.add("aCourse");
        validTagsList.add("aCourse");

        IllegalStateException exception = null;
        try {
            tagService.checkTagsListValidity(validTagsList);
        } catch (IllegalStateException e) {
            exception = e;
        }

        Assertions.assertEquals("Invalid tags list (contains duplicate tags)", exception.getMessage());
    }

}
