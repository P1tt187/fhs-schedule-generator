package models.persistence.lecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.persistence.Docent;
import models.persistence.criteria.CriteriaContainer;
import models.persistence.enumerations.EDuration;
import models.persistence.enumerations.ELectureKind;
import models.persistence.location.RoomEntity;
import models.persistence.participants.Participant;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author fabian
 *         on 28.01.14.
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

    /**
     * same subject can have diffrent names in diffrent courses
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @MapKeyColumn(name = "COURSE")
    @Column(name = "LECTURENAME")
    @CollectionTable(name = "TBLLECTURE_SYNONYMS")
    private Map<String, String> lectureSynonyms;

    /**
     * kind of this lecture
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "KIND")
    private ELectureKind kind;

    /**
     * number of exprected participants
     */
    @Column(name = "EXPECTED_PARTICIPANTS")
    private Integer expectedParticipants;

    @Transient
    @JsonIgnore
    private Integer difficultLevel = 0;

    public void setDifficultLevel(Integer difficultLevel) {
        this.difficultLevel = difficultLevel;
    }

    public void increaseDifficultLevel() {
        if (difficultLevel == 0 || difficultLevel == 1) {
            difficultLevel++;
        }
        difficultLevel *= difficultLevel;
    }

    /**
     * if expected participants is set it will return it
     * else it will sum the size of all participants
     */
    public Integer calculateNumberOfParticipants() {
        if (expectedParticipants != null && expectedParticipants > 0) {
            return expectedParticipants;
        }
        return participants.stream().mapToInt(Participant::getSize).sum();
    }

    /**
     * calculate cost of this lecture
     * the more costs a lecture have it will be more important to place it first
     */
    @JsonIgnore
    public Integer getCosts() {
        Integer ret = 0;

        if (criteriaContainer != null) {
            ret += criteriaContainer.getCost();
        }

        if (docents != null) {
            ret += docents.parallelStream().mapToInt(d -> d.getCriteriaContainer().getCost()).sum();
        }

        if (participants != null) {
            ret += participants.size();
        }

        ret += calculateNumberOfParticipants();

        ret += difficultLevel;

        ret += duration.getSortIndex();

        return ret;
    }

    public Integer getExpectedParticipants() {
        return expectedParticipants;
    }

    public void setExpectedParticipants(Integer expectedParticipants) {
        this.expectedParticipants = expectedParticipants;
    }

    public ELectureKind getKind() {
        return kind;
    }

    public void setKind(ELectureKind kind) {
        this.kind = kind;
    }

    public Map<String, String> getLectureSynonyms() {
        return lectureSynonyms;
    }

    public void setLectureSynonyms(Map<String, String> lectureSynonyms) {
        this.lectureSynonyms = lectureSynonyms;
    }

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
        return new HashSet<RoomEntity>() {{
            add(room);
        }};
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
        if (expectedParticipants != null ? !expectedParticipants.equals(lecture.expectedParticipants) : lecture.expectedParticipants != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (docents != null ? docents.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        result = 31 * result + (expectedParticipants != null ? expectedParticipants.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Lecture{");
        sb.append("name='").append(name).append('\'');
        sb.append(", kind=").append(kind);
        sb.append(", expectedParticipants=").append(expectedParticipants);
        sb.append(", participants=").append(participants);
        sb.append(", docents=").append(docents);
        sb.append(", room=").append(room);
        sb.append(", duration=").append(duration);
        sb.append(", criteriaContainer=").append(criteriaContainer);
        sb.append('}');
        return sb.toString();
    }


}
