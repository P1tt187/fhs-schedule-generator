package models.persistence.lecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.persistence.criteria.CriteriaContainer;
import models.persistence.docents.LectureDocent;
import models.persistence.enumerations.EDuration;
import models.persistence.enumerations.ELectureKind;
import models.persistence.location.RoomEntity;
import models.persistence.participants.Participant;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.math.BigInteger;
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
    @ManyToMany( fetch = FetchType.EAGER, targetEntity = Participant.class )
    @Fetch(FetchMode.SUBSELECT)
    private Set<Participant> participants;

    /**
     * docents for this lecture
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="TBLLECTURE_DOCENTS")
    private Set<LectureDocent> docents;
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
    @Transient
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
    private BigInteger difficultLevel = BigInteger.ZERO;

    /**
     * this is just for debugging
     *
     * by setting 'schedule.show.outOfWishTime=on' it will be honored by the schedule presentation
     *
     */
    @Transient
    private String notOptimalPlaced = "";


    public String getNotOptimalPlaced() {
        return notOptimalPlaced;
    }

    public void setNotOptimalPlaced(String notOptimalPlaced) {
        this.notOptimalPlaced = notOptimalPlaced;
    }

    public void setDifficultLevel(BigInteger difficultLevel) {
        this.difficultLevel = difficultLevel;
    }

    public void increaseDifficultLevel() {
        difficultLevel = difficultLevel.add(getDifficulty());
    }

    /**
     * if expected participants is set it will return it
     * else it will sum the size of all participants
     */
    public Integer calculateNumberOfParticipants() {
        if (expectedParticipants != null && expectedParticipants > 0) {
            return expectedParticipants;
        }

        /** it is expected, that 10% of the participants will not visit this lecture */
        int ret = participants.stream().mapToInt(Participant::getSize).sum();
        ret -= Math.floor(((double) ret) * 10.0 / 100.0);
        return ret;
    }

    /**
     * calculate cost of this lecture
     * the more costs a lecture have it will be more important to place it first
     */
    @JsonIgnore
    public BigInteger getDifficulty() {
        BigInteger ret = BigInteger.ZERO;

        if (criteriaContainer != null) {
            ret = ret.add(BigInteger.valueOf(criteriaContainer.calculateDifficultLevel()));
        }

        if (docents != null) {
            ret = ret.add(BigInteger.valueOf(docents.parallelStream().mapToLong(d -> d.getCriteriaContainer().calculateDifficultLevel()).sum()));
        }

        if (participants != null) {
            ret = ret.add(BigInteger.valueOf(participants.size()));
        }

        ret = ret.add(BigInteger.valueOf(calculateNumberOfParticipants()));

        ret = ret.add(difficultLevel);

        ret = ret.add(BigInteger.valueOf(duration.getSortIndex()));

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

    public Set<LectureDocent> getDocents() {
        return docents;
    }

    public void setDocents(Set<LectureDocent> docent) {
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

        if (difficultLevel != null ? !difficultLevel.equals(lecture.difficultLevel) : lecture.difficultLevel != null)
            return false;
        return !(expectedParticipants != null ? !expectedParticipants.equals(lecture.expectedParticipants) : lecture.expectedParticipants != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (docents != null ? docents.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        result = 31 * result + (expectedParticipants != null ? expectedParticipants.hashCode() : 0);
        result = 31 * result + (difficultLevel != null ? difficultLevel.hashCode() : 0);
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
