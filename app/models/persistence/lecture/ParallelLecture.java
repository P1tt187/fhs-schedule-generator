package models.persistence.lecture;

import models.persistence.docents.LectureDocent;
import models.persistence.enumerations.EDuration;
import models.persistence.location.LectureRoom;
import models.persistence.location.RoomEntity;
import models.persistence.participants.LectureParticipant;
import models.persistence.participants.Participant;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author fabian
 *         on 28.01.14.
 */
@Entity
@Table(name = "TBLLECTURE_PARALLEL")
public class ParallelLecture extends AbstractLecture {

    /**
     * lectures in this parallel lecture
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Lecture.class)
    private List<Lecture> lectures;

    public List<Lecture> getLectures() {
        return lectures;
    }

    public void setLectures(List<Lecture> lectures) {
        this.lectures = lectures;
    }

    @Override
    public Set<LectureDocent> getDocents() {
        return new HashSet<LectureDocent>() {{
            for (Lecture l : lectures) {
                addAll(l.getDocents());
            }
        }};
    }

    @Override
    public Set<Participant> getParticipants() {
        return new HashSet<Participant>() {{
            for (Lecture l : lectures) {
                addAll(l.getParticipants());
            }
        }};
    }

    @Override
    public Set<LectureParticipant> getLectureParticipants() {
        return lectures.stream().flatMap(l -> l.getLectureParticipants().stream()).collect(Collectors.toSet());
    }

    @Override
    public EDuration getDuration() {
        return null;
    }

    @Override
    public Set<LectureRoom> getRooms() {
        return new HashSet<LectureRoom>() {{
            for (Lecture l : lectures) {
                addAll(l.getRooms());
            }
        }};
    }

    @Override
    public Set<RoomEntity> getRoomEntitys(){
        return new HashSet<RoomEntity>(){{
            for(Lecture l : lectures){
                addAll(l.getRoomEntitys());
            }
        }};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ParallelLecture that = (ParallelLecture) o;

        return !(lectures != null ? !lectures.equals(that.lectures) : that.lectures != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lectures != null ? lectures.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ParallelLecture{");
        sb.append("lectures=").append(lectures);
        sb.append('}');
        return sb.toString();
    }
}
