package models.persistence.docents;

import models.persistence.criteria.CriteriaContainer;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Helper class to handle database storing docent
 * this is needed to prevent hibernate from deleting connection between subject and docent
 *
 * @author fabian
 *         on 21.05.14.
 */
@Embeddable
public class LectureDocent implements Comparable<Docent> {

    /**
     * name of the docent
     */
    @Column(name = "LASTNAME", nullable = false)
    private String lastName;

    /**
     * userid can also stored in the displayed schedule
     */
    @Column(name = "USERID")
    private String userId;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public CriteriaContainer getCriteriaContainer() {
        return criteriaContainer;
    }

    public void setCriteriaContainer(CriteriaContainer criteriaContainer) {
        this.criteriaContainer = criteriaContainer;
    }

    public LectureDocent() {
    }

    public LectureDocent(String lastName, String userId, CriteriaContainer criteriaContainer) {
        this.lastName = lastName;
        this.userId = userId;
        this.criteriaContainer = criteriaContainer;
    }

    /**
     * contains all criterias
     */
    @Transient
    private CriteriaContainer criteriaContainer;


    @Override
    public int compareTo(Docent that) {
        return this.getLastName().compareTo(that.getLastName());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("LectureDocent{");
        sb.append("lastName='").append(lastName).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LectureDocent that = (LectureDocent) o;

        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return lastName != null ? lastName.hashCode() : 0;
    }
}
