package models.persistence;

import models.persistence.criteria.CriteriaContainer;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * Created by fabian on 01.02.14.
 */
@Entity
@Table(name = "TBLDOCENT")
public class Docent extends AbstractEntity {


    @Column(name = "FIRSTNAME")
    @Constraints.Required
    private String firstName;

    @Column(name = "LASTNAME")
    @Constraints.Required
    private String lastName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Docent docent = (Docent) o;

        if (firstName != null ? !firstName.equals(docent.firstName) : docent.firstName != null) return false;
        if (lastName != null ? !lastName.equals(docent.lastName) : docent.lastName != null) return false;

        return true;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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
    @JoinTable(name = "fk_critcontainer")
    @OneToOne(cascade = CascadeType.ALL)
    @Constraints.Required
    private CriteriaContainer criteriaContainer;

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        return result;
    }
}
