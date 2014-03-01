package models.persistence.participants;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@Table(name = "TBLCOURSE")
public class Course extends Participant {

    @Column(name = "FULLNAME", nullable = false)
    private String fullName;

    @Column(name="SHORTNAME", nullable = false)
    private String shortName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    private List<Group> groups;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;

        Course course = (Course) o;

        if (fullName != null ? !fullName.equals(course.fullName) : course.fullName != null) return false;
        if (shortName != null ? !shortName.equals(course.shortName) : course.shortName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fullName != null ? fullName.hashCode() : 0;
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Course{" +
                "fullName='" + fullName + '\'' +
                ", shortName='" + shortName + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}
