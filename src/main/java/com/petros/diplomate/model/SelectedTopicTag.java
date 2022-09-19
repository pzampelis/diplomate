package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class SelectedTopicTag extends SelectedTag {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "topic_id")
    private Topic topic;


    public SelectedTopicTag(boolean isSelected, Tag tag, Topic topic) {
        super(isSelected, tag, "TOPIC_TYPE");
        this.topic = topic;
    }

}
