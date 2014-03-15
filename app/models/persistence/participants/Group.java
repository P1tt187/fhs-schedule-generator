package models.persistence.participants;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@Table(name="TBLGROUP")
public class Group extends Participant {

    /** parent group */
    @ManyToOne()
    private Group parent;

    /** subgroups of this group*/
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "parent")
    private List<Group> subGroups;

    /** parent course */
    @ManyToOne(cascade = CascadeType.ALL)
    private Course course;

    /** type of this group*/
    @Column(name="TYPE")
    private String groupType;

    public Group getParent() {
        return parent;
    }

    public void setParent(Group parent) {
        this.parent = parent;
    }

    public List<Group> getSubGroups() {
        return subGroups;
    }

    public void setSubGroups(List<Group> subGroups) {
        this.subGroups = subGroups;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        if (course != null ? !course.equals(group.course) : group.course != null) return false;
        if (groupType != null ? !groupType.equals(group.groupType) : group.groupType != null) return false;
        if (parent != null ? !parent.equals(group.parent) : group.parent != null) return false;
        if (subGroups != null ? !subGroups.equals(group.subGroups) : group.subGroups != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent != null ? parent.hashCode() : 0;
        result = 31 * result + (subGroups != null ? subGroups.hashCode() : 0);
        result = 31 * result + (course != null ? course.hashCode() : 0);
        result = 31 * result + (groupType != null ? groupType.hashCode() : 0);
        return result;
    }
}
