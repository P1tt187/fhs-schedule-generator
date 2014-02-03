package models.persistence.participants;

import models.persistence.AbstractEntity;
import models.persistence.lecture.Lecture;
import org.hibernate.annotations.DiscriminatorOptions;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@Table(name="TBLParticipants")
@DiscriminatorOptions(force = true)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Participant extends AbstractEntity{

    @Column(name="SIZE", nullable = false)
    @Constraints.Required
    public Integer size;

    @ManyToOne
    public Lecture lecture;
}
