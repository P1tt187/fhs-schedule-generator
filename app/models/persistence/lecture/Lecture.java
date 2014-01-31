package models.persistence.lecture;

import models.persistence.participants.Participant;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLLECTURE")
public class Lecture extends AbstractLecture {

    @Column(name = "NAME", nullable = false)
    @Constraints.Required
    public String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lecture")
    public List<Participant> participants;
}
