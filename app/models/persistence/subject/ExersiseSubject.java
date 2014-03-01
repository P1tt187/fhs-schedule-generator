package models.persistence.subject;

import models.persistence.participants.Course;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Table(name="TBLEXERSISE_SUBJECT")
public class ExersiseSubject extends AbstractSubject {


    @Column(name = "GROUPTYPE")
    private String groupType;

    @OneToOne(targetEntity = Course.class)
    private Course course;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }


}
