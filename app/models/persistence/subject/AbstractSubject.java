package models.persistence.subject;

import models.persistence.AbstractEntity;
import models.persistence.Docent;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
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
    @Column(name = "ACTIVE")
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
     * docents for this subject
     */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = Docent.class, fetch = FetchType.EAGER)
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
                "active=" + active +
                ", units=" + units +
                ", name='" + name + '\'' +
                ", docents=" + docents +
                ", subjectSynonyms=" + subjectSynonyms +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractSubject that = (AbstractSubject) o;

        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (docents != null ? !docents.equals(that.docents) : that.docents != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
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
        result = 31 * result + (subjectSynonyms != null ? subjectSynonyms.hashCode() : 0);
        result = 31 * result + (docents != null ? docents.hashCode() : 0);
        return result;
    }
}
