package models.persistence.lecture;

import models.persistence.Docent;
import models.persistence.enumerations.EDuration;
import models.persistence.participants.Participant;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLLECTURE")
public class Lecture extends AbstractLecture {

    /** name of the lecture */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** participants can be multiple courses or groups*/
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Participant.class)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Participant> participants;

    @OneToMany(targetEntity = Docent.class)
    private Set<Docent> docents;


    @Enumerated(EnumType.STRING)
    @Column(name = "DURATION", nullable = false)
    private EDuration duration;

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
}
