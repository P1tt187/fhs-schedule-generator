package models.persistence.subject;

import models.persistence.participants.Course;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
public class ExersiseSubject extends AbstractSubject {

    @Constraints.Required
    @Column(name = "GROUPTYPE")
    private String groupType;


    @OneToOne(targetEntity = Course.class)
    private Course course;

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
