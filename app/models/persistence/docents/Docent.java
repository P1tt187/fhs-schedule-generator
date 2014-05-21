package models.persistence.docents;

import models.persistence.AbstractEntity;
import models.persistence.criteria.CriteriaContainer;

import javax.persistence.*;

/**
 * @author fabian
 *         on 01.02.14.
 */
@Entity
@Table(name = "TBLDOCENT")
public class Docent extends AbstractEntity implements Comparable<Docent> {


    /**
     * name of the docent
     */
    @Column(name = "LASTNAME", nullable = false)
    private String lastName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public CriteriaContainer getCriteriaContainer() {
        return criteriaContainer;
    }

    public void setCriteriaContainer(CriteriaContainer criteriaContainer) {
        this.criteriaContainer = criteriaContainer;
    }

    /**
     * contains all criterias
     */
    @OneToOne(cascade = CascadeType.ALL, targetEntity = CriteriaContainer.class)
    private CriteriaContainer criteriaContainer;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Docent docent = (Docent) o;

        if (lastName != null ? !lastName.equals(docent.lastName) : docent.lastName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return lastName != null ? lastName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Docent{" +
                "lastName='" + lastName + '\'' +
                '}';
    }


    public LectureDocent docent2LectureDocent() {
        return new LectureDocent(lastName, criteriaContainer);
    }

    @Override
    public int compareTo(Docent that) {
        return this.lastName.compareTo(that.lastName);
    }
}
