package models.persistence.lecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.persistence.AbstractEntity;
import models.persistence.Docent;
import models.persistence.location.RoomEntity;
import models.persistence.participants.Participant;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="TBLALECTURE")
public abstract class AbstractLecture extends AbstractEntity {


    @JsonIgnore
    public abstract Set<Docent> getDocents();

    @JsonIgnore
    public abstract Set<Participant> getParticipants();

    @JsonIgnore
    public abstract Set<RoomEntity> getRooms();

}
