package models.persistence.subject;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import models.persistence.AbstractEntity;
import models.persistence.Semester;
import models.persistence.criteria.CriteriaContainer;
import models.persistence.docents.Docent;
import models.persistence.enumerations.EDuration;
import models.persistence.location.RoomEntity;
import models.persistence.participants.Course;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author fabian
 *         on 07.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLSUBJECT")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractSubject extends AbstractEntity {

    /**
     * units of this subject, 0.5 means not weekly
     */
    @Column(name = "UNITS", nullable = false)
    private Float units;

    /**
     * name of this subject
     */
    @Column(name = "NAME", nullable = false)
    private String name;

    /**
     * is this subject used in schedule
     */
    @Column(name = "ACTIVE", nullable = false)
    private Boolean active;

    /**
     * which semester is it
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    @ManyToOne(targetEntity = Semester.class, optional = false, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Semester semester;

    /**
     * same subject can have diffrent names in diffrent courses
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @MapKeyColumn(name = "COURSE")
    @Column(name = "SUBJECTNAME")
    @CollectionTable(name = "TBLSYNONYMS")
    private Map<String, String> subjectSynonyms;


    /**
     * the courses
     */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = Course.class, fetch = FetchType.EAGER)
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    private Set<Course> courses;


    /**
     * criterias of this subject
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    @ManyToOne(targetEntity = CriteriaContainer.class, cascade = CascadeType.ALL)
    private CriteriaContainer criteriaContainer;

    /**
     * number of exprected participants
     */
    @Column(name = "EXPECTED_PARTICIPANTS")
    private Integer expectedParticipants;

    /** if duration is not weekly the system will try to split the units */
    @Column(name = "duration")
    @Enumerated(EnumType.STRING)
    private EDuration duration;

    /** alternative rooms for this subject  */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = RoomEntity.class, fetch = FetchType.EAGER)
    private List<RoomEntity> alternativRooms;

    public List<RoomEntity> getAlternativRooms() {
        return alternativRooms;
    }

    public void setAlternativRooms(List<RoomEntity> alternativRooms) {
        this.alternativRooms = alternativRooms;
    }

    public EDuration getDuration() {
        return duration;
    }

    public void setDuration(EDuration duration) {
        this.duration = duration;
    }

    public Integer getExpectedParticipants() {
        return expectedParticipants;
    }

    public void setExpectedParticipants(Integer expectedParticipants) {
        this.expectedParticipants = expectedParticipants;
    }

    public CriteriaContainer getCriteriaContainer() {
        return criteriaContainer;
    }

    public void setCriteriaContainer(CriteriaContainer criteriaContainer) {
        this.criteriaContainer = criteriaContainer;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    public Boolean isActive() {
        return active;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Map<String, String> getSubjectSynonyms() {
        return subjectSynonyms;
    }

    public void setSubjectSynonyms(Map<String, String> subjectSynonyms) {
        this.subjectSynonyms = subjectSynonyms;
    }

    /**
     * docents for this subject
     */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = Docent.class, fetch = FetchType.EAGER)
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    private Set<Docent> docents;

    public Float getUnits() {
        return units;
    }

    public void setUnits(Float units) {
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Docent> getDocents() {
        return docents;
    }

    public void setDocents(Set<Docent> docents) {
        this.docents = docents;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
        sb.append("{units=").append(units);
        sb.append(", name='").append(name).append('\'');
        sb.append(", expectedParticipants=").append(expectedParticipants);
        sb.append(", active=").append(active);
        sb.append(", semester=").append(semester);
        sb.append(", subjectSynonyms=").append(subjectSynonyms);
        sb.append(", courses=").append(courses);
        sb.append(", criteriaContainer=").append(criteriaContainer);
        sb.append(", expectedParticipants=").append(expectedParticipants);
        sb.append(", docents=").append(docents);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractSubject that = (AbstractSubject) o;

        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (courses != null ? !courses.equals(that.courses) : that.courses != null) return false;
        if (criteriaContainer != null ? !criteriaContainer.equals(that.criteriaContainer) : that.criteriaContainer != null)
            return false;
        if (docents != null ? !docents.equals(that.docents) : that.docents != null) return false;
        if (expectedParticipants != null ? !expectedParticipants.equals(that.expectedParticipants) : that.expectedParticipants != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (semester != null ? !semester.equals(that.semester) : that.semester != null) return false;
        if (subjectSynonyms != null ? !subjectSynonyms.equals(that.subjectSynonyms) : that.subjectSynonyms != null)
            return false;
        if (units != null ? !units.equals(that.units) : that.units != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = units != null ? units.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (semester != null ? semester.hashCode() : 0);
        result = 31 * result + (subjectSynonyms != null ? subjectSynonyms.hashCode() : 0);
        result = 31 * result + (courses != null ? courses.hashCode() : 0);
        result = 31 * result + (criteriaContainer != null ? criteriaContainer.hashCode() : 0);
        result = 31 * result + (expectedParticipants != null ? expectedParticipants.hashCode() : 0);
        result = 31 * result + (docents != null ? docents.hashCode() : 0);
        return result;
    }
}
