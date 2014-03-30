package models.persistence.participants;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@Table(name = "TBLGROUP")
public class Group extends Participant {

    /**
     * parent group
     */
    @JsonBackReference("subgroup")
    @ManyToOne(targetEntity = Group.class)
    private Group parent;

    /**
     * subgroups of this group
     */
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "parent")
    @JsonManagedReference("subgroup")
    private List<Group> subGroups;

    /**
     * parent course
     */
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@group-course")
    @ManyToOne(targetEntity = Course.class)
    private Course course;

    /**
     * type of this group
     */
    @Column(name = "TYPE")
    private String groupType;

    /**
     * index of this group
     */
    @Column(name = "GROUPINDEX")
    private Integer groupIndex;

    public Integer getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Integer groupIndex) {
        this.groupIndex = groupIndex;
    }

    @Override
    public String getName() {
        return course.getShortName() + " " + groupIndex;
    }

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

    @Override
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
    public String toString() {
        final StringBuffer sb = new StringBuffer("Group{");
        sb.append("parent=").append(parent != null ? parent.getGroupType() : null);
        sb.append(", subGroups=").append(subGroups);
        sb.append(", course=").append(course);
        sb.append(", groupType='").append(groupType).append('\'');
        sb.append(", groupIndex=").append(groupIndex);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        if (course != null ? !course.equals(group.course) : group.course != null) return false;
        if (groupType != null ? !groupType.equals(group.groupType) : group.groupType != null) return false;
        if (groupIndex != null ? !groupIndex.equals(group.groupIndex) : group.groupIndex != null) return false;
        if (subGroups != null ? !(subGroups.containsAll(group.subGroups) && subGroups.size() == group.subGroups.size()) : group.subGroups != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subGroups != null ? subGroups.hashCode() : 0;
        result = 31 * result + (course != null ? course.hashCode() : 0);
        result = 31 * result + (groupType != null ? groupType.hashCode() : 0);
        result = 31 * result + (groupIndex != null ? groupIndex.hashCode() : 0);

        if (subGroups != null) {
            for (Group subGroup : subGroups) {
                result = 31 * result + subGroup.hashCode();
            }
        }
        return result;
    }
}
