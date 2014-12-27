package models.persistence.participants;

import models.persistence.location.LectureRoom;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian
 *      on 29.01.14.
 */
@Entity
@Table(name = "TBLCOURSE")
public class Course extends Participant {
    /**
     * full name
     * e.g. Bachelor Informatik
     */
    @Column(name = "FULLNAME", nullable = false)
    private String fullName;
    /**
     * short name of this course
     * e.g. BAI1
     */
    @Column(name = "SHORTNAME", nullable = false)
    private String shortName;

    /**
     * exersize groups
     */
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(cascade = CascadeType.ALL, targetEntity = Group.class, fetch = FetchType.LAZY, mappedBy = "course", orphanRemoval=true)
    private List<Group> groups;

    /**
     * preferd room for this course
     */
    private LectureRoom classRoom;

    public LectureRoom getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(LectureRoom classRoom) {
        this.classRoom = classRoom;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public String getName() {
        return shortName;
    }

    @Override
    public LectureParticipant participant2LectureParticipant() {
        LectureParticipant lp = new LectureParticipant();
        lp.setCourseName(shortName);
        lp.setIgnoreGroupIndex(true);
        return lp;
    }

    @Override
    public Course getCourse() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Course course = (Course) o;

        if (fullName != null ? !fullName.equals(course.fullName) : course.fullName != null) return false;

        if (shortName != null ? !shortName.equals(course.shortName) : course.shortName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fullName != null ? fullName.hashCode() : 0;
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Course{");
        sb.append("fullName='").append(fullName).append('\'');
        sb.append(", shortName='").append(shortName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
