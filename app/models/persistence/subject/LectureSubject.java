package models.persistence.subject;

import models.persistence.participants.Course;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * @author fabian
 *         on 07.02.14.
 */
@Entity
@Table(name = "TBLLECTURE_SUBJECT")
public class LectureSubject extends AbstractSubject {


    /**
     * the courses
     */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = Course.class, fetch = FetchType.EAGER)
    private Set<Course> courses;

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    @Override
    public String toString() {
        return super.toString() + "courses=" + courses;
    }

}
