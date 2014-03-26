package models.persistence.lecture;

import models.persistence.Docent;
import models.persistence.criteria.CriteriaContainer;
import models.persistence.enumerations.EDuration;
import models.persistence.location.RoomEntity;
import models.persistence.participants.Participant;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLLECTURE")
public class Lecture extends AbstractLecture {

    /**
     * name of the lecture
     */
    @Column(name = "NAME", nullable = false)
    private String name;

    /**
     * participants can be multiple courses or groups
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Participant.class)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Participant> participants;

    /**
     * docents for this lecture
     */
    @OneToMany(targetEntity = Docent.class)
    private Set<Docent> docents;
    /**
     * room for this lecture
     */
    @ManyToOne(targetEntity = RoomEntity.class)
    private RoomEntity room;

    /**
     * duration of this lecture
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "DURATION", nullable = false)
    private EDuration duration;

    /**
     * criterias of this lecture
     */
    @ManyToOne(targetEntity = CriteriaContainer.class)
    private CriteriaContainer criteriaContainer;

    public CriteriaContainer getCriteriaContainer() {
        return criteriaContainer;
    }

    public void setCriteriaContainer(CriteriaContainer criteriaContainer) {
        this.criteriaContainer = criteriaContainer;
    }

    public RoomEntity getRoom() {
        return room;
    }

    public void setRoom(RoomEntity room) {
        this.room = room;
    }

    public EDuration getDuration() {
        return duration;
    }

    public void setDuration(EDuration duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    public Set<Docent> getDocents() {
        return docents;
    }

    public void setDocents(Set<Docent> docent) {
        this.docents = docent;
    }

    @Override
    public Set<RoomEntity> getRooms() {
        return new HashSet<RoomEntity>(){{ add(room); }} ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lecture)) return false;

        Lecture lecture = (Lecture) o;

        if (docents != null ? !docents.equals(lecture.docents) : lecture.docents != null) return false;
        if (duration != lecture.duration) return false;
        if (name != null ? !name.equals(lecture.name) : lecture.name != null) return false;
        if (participants != null ? !participants.equals(lecture.participants) : lecture.participants != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (docents != null ? docents.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Lecture{");
        sb.append("name='").append(name).append('\'');
        sb.append(", participants=").append(participants);
        sb.append(", docents=").append(docents);
        sb.append(", room=").append(room);
        sb.append(", duration=").append(duration);
        sb.append(", criteriaContainer=").append(criteriaContainer);
        sb.append('}');
        return sb.toString();
    }


}
