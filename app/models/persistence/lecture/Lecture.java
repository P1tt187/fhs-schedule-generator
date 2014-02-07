package models.persistence.lecture;

import models.persistence.Docent;
import models.persistence.enumerations.EDuration;
import models.persistence.participants.Participant;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLLECTURE")
public class Lecture extends AbstractLecture {

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lecture")
    private List<Participant> participants;

    @OneToOne(optional = false, targetEntity = Docent.class)
    private Docent docent;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration", nullable = false)
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

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public Docent getDocent() {
        return docent;
    }

    public void setDocent(Docent docent) {
        this.docent = docent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lecture)) return false;

        Lecture lecture = (Lecture) o;

        if (docent != null ? !docent.equals(lecture.docent) : lecture.docent != null) return false;
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
        result = 31 * result + (docent != null ? docent.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        return result;
    }
}
