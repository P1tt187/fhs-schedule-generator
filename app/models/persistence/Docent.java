package models.persistence;

import models.persistence.criteria.CriteriaContainer;

import javax.persistence.*;

/**
 * Created by fabian on 01.02.14.
 */
@Entity
@Table(name = "TBLDOCENT")
public class Docent extends AbstractEntity {


    /**
     * name of the docent
     */
    @Column(name = "LASTNAME", nullable = false)
    private String lastName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Docent docent = (Docent) o;


        if (lastName != null ? !lastName.equals(docent.lastName) : docent.lastName != null) return false;

        return true;
    }

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
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Docent{" +
                "lastName='" + lastName + '\'' +
                '}';
    }
}
