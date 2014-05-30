package models.persistence.participants;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author fabian
 *         on 30.05.14.
 *
 *         just a wrapper class to prevent database conflicts when deleting groups in existing view
 */

@Embeddable
public class LectureParticipant {
/** name of the course*/
    @Column(name = "COURSENAME")
    private String courseName;

    @Column(name = "GROUPINDEX")
    private Integer groupIndex;

    @Column(name="IGNORE_GROUPINDEX")
    private Boolean ignoreGroupIndex;

    public LectureParticipant() {
    }


    public Boolean isIgnoreGroupIndex() {
        return ignoreGroupIndex;
    }

    public void setIgnoreGroupIndex(Boolean ignoreGroupIndex) {
        this.ignoreGroupIndex = ignoreGroupIndex;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Integer groupIndex) {
        this.groupIndex = groupIndex;
    }

    public String getShortName() {
        StringBuffer sb = new StringBuffer();
        sb.append(courseName);
        if (!ignoreGroupIndex) {
            sb.append(" ");
            sb.append(groupIndex);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LectureParticipant that = (LectureParticipant) o;

        if (courseName != null ? !courseName.equals(that.courseName) : that.courseName != null) return false;
        if (groupIndex != null ? !groupIndex.equals(that.groupIndex) : that.groupIndex != null) return false;
        if (ignoreGroupIndex != null ? !ignoreGroupIndex.equals(that.ignoreGroupIndex) : that.ignoreGroupIndex != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = courseName != null ? courseName.hashCode() : 0;
        result = 31 * result + (groupIndex != null ? groupIndex.hashCode() : 0);
        result = 31 * result + (ignoreGroupIndex != null ? ignoreGroupIndex.hashCode() : 0);
        return result;
    }
}
