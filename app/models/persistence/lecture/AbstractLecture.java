package models.persistence.lecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.persistence.AbstractEntity;
import models.persistence.docents.LectureDocent;
import models.persistence.enumerations.EDuration;
import models.persistence.location.RoomEntity;
import models.persistence.participants.LectureParticipant;
import models.persistence.participants.Participant;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.Set;

/**
 * @author fabian
 *         on 28.01.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLLECTURE_ABSTRACT")
public abstract class AbstractLecture extends AbstractEntity {


    @JsonIgnore
    public abstract Set<LectureDocent> getDocents();

    @JsonIgnore
    public abstract Set<Participant> getParticipants();

    @JsonIgnore
    public abstract Set<LectureParticipant> getLectureParticipants();

    @JsonIgnore
    public abstract EDuration getDuration();

    @JsonIgnore
    public abstract Set<RoomEntity> getRooms();

}
