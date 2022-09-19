package com.petros.diplomate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SelectedTag {

    @Id
    @SequenceGenerator(name = "selected_tag_sequence", sequenceName = "selected_tag_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "selected_tag_sequence")
    private Long id;

    private boolean isSelected;
    private String type;

    @OneToOne
    @JoinColumn(nullable = false, name = "tag_id")
    private Tag tag;


    public SelectedTag(boolean isSelected, Tag tag, String type) {
        this.isSelected = isSelected;
        this.tag = tag;
        this.type = type;
    }

}
