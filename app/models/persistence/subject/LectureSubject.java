package models.persistence.subject;

import models.persistence.participants.Course;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
public class LectureSubject extends AbstractSubject{
    @OneToMany(targetEntity = Course.class)
    private List<Course> courses;

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
