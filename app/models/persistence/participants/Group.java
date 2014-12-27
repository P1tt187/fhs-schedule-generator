package models.persistence.participants;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * <p> Groups are Participants for Exercise </p>
 *
 * @author fabian
 *         on 29.01.14.
 */
@Entity
@Table(name = "TBLGROUP")
public class Group extends Participant implements Comparable<Group> {

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

    /**
     * flag for view
     */
    @Column(name = "IGNORE_GROUPINDEX")
    private Boolean ignoreGroupIndex;

    public Boolean isIgnoreGroupIndex() {
        return ignoreGroupIndex;
    }

    public void setIgnoreGroupIndex(Boolean ignoreGroupIndex) {
        this.ignoreGroupIndex = ignoreGroupIndex;
    }

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

    @Override
    public LectureParticipant participant2LectureParticipant() {

        LectureParticipant lp = new LectureParticipant();
        lp.setCourseName(course.getShortName());
        lp.setGroupIndex(groupIndex);
        boolean ignoreValue = ignoreGroupIndex != null ? ignoreGroupIndex : false;
        lp.setIgnoreGroupIndex(ignoreValue);

        return lp;
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

        sb.append(", groupType='").append(groupType).append('\'');
        sb.append(", groupIndex=").append(groupIndex);
        sb.append(", course=").append(course.getShortName() != null ? course.getShortName() : "");

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


        return true;
    }

    @Override
    public int hashCode() {

        int result = 31 * (course != null ? course.hashCode() : 0);
        result = 31 * result + (groupType != null ? groupType.hashCode() : 0);
        result = 31 * result + (groupIndex != null ? groupIndex.hashCode() : 0);


        return result;
    }

    @Override
    public int compareTo(Group that) {
        if (groupType.compareTo(that.groupType) != 0) {
            return groupType.compareTo(that.groupType);
        }
        return groupIndex.compareTo(that.groupIndex);
    }


}
