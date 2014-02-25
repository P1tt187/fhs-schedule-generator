package models.persistence.subject;

import models.persistence.AbstractEntity;
import models.persistence.Docent;
import models.persistence.participants.Participant;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "TBLSUBJECT")
public abstract class AbstractSubject extends AbstractEntity {

    /**
     * units of this subject, 0.5 means not weekly
     */
    @Column(name = "UNITS", nullable = true)
    private Float units;

    /**
     * name of this subject
     */
    @Column(name = "NAME")
    private String name;

    /**
     * is this subject used in schedule
     */
    @Column(name = "active")
    private Boolean active;


    /**
     * same subject can have diffrent names in diffrent courses
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @MapKeyColumn(name = "COURSE")
    @Column(name = "SUBJECTNAME")
    private Map<String, String> subjectSynonyms;

    public Boolean isActive() {
        return active;
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
     * the courses
     */
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = Participant.class, fetch = FetchType.EAGER)
    private Set<Participant> participants;

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> courses) {
        this.participants = courses;
    }

    /**
     * docents for this subject
     */
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = Docent.class, fetch = FetchType.EAGER)
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
        return this.getClass().getSimpleName() +
                "{" +
                "active=" +
                ", units=" + units +
                ", name='" + name + '\'' +
                ", participants=" + participants +
                ", docents=" + docents +
                ", subjectSynonyms=" + subjectSynonyms +
                '}';
    }
}
