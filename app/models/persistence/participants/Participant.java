package models.persistence.participants;

import models.persistence.lecture.Lecture;
import org.hibernate.annotations.DiscriminatorOptions;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@Table(name="TBLParticipants")
@DiscriminatorOptions(force = true)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Participant extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(name="SIZE", nullable = false)
    @Constraints.Required
    public Integer size;

    @ManyToOne
    public Lecture lecture;
}
