package models.persistence.lecture;

import models.persistence.Docent;
import models.persistence.location.RoomEntity;
import models.persistence.participants.Participant;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLPARALLELLECTURE")
public class ParallelLecture extends AbstractLecture {

    // @JoinColumn(name = "fk_lecture")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Lecture.class)
    private List<Lecture> lectures;

    public List<Lecture> getLectures() {
        return lectures;
    }

    public void setLectures(List<Lecture> lectures) {
        this.lectures = lectures;
    }

    @Override
    public Set<Docent> getDocents() {
        return new HashSet<Docent>() {{
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
    public Set<RoomEntity> getRooms() {
        return new HashSet<RoomEntity>() {{
            for (Lecture l : lectures) {
                add(l.getRoom());
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
