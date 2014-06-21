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

    /**
     * docents can tell their wishes to the admin by fill out comment field
     */
    @Column(name = "COMMENTS")
    private String comments;

    /**
     * needed if docent name differs with loginname
     */
    @Column(name = "USERID")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Docent docent = (Docent) o;

        if (comments != null ? !comments.equals(docent.comments) : docent.comments != null) return false;
        if (lastName != null ? !lastName.equals(docent.lastName) : docent.lastName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lastName != null ? lastName.hashCode() : 0;
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Docent{" +
                "lastName='" + lastName + '\'' +
                '}';
    }


    public LectureDocent docent2LectureDocent() {
        return new LectureDocent(lastName, userId, criteriaContainer);
    }

    @Override
    public int compareTo(Docent that) {
        return this.lastName.compareTo(that.lastName);
    }
}
