package models.persistence.template;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import models.persistence.AbstractEntity;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Table(name = "TBLWEEKDAY_TEMPLATE")
public class WeekdayTemplate extends AbstractEntity implements Comparable<WeekdayTemplate> {

    @Constraints.Required
    @Column(name = "NAME")
    private String name;

    @Constraints.Required
    @Column(name = "SORTINDEX")
    private Integer sortIndex;

    @JsonManagedReference("timeslotTemplate")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.EAGER)
    private List<TimeSlotTemplate> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public List<TimeSlotTemplate> getChildren() {
        return children;
    }

    public void setChildren(List<TimeSlotTemplate> timeSlotTemplates) {
        this.children = timeSlotTemplates;
    }

    public WeekdayTemplate(String name, Integer sortIndex) {
        this.name = name;
        this.sortIndex = sortIndex;
        this.children = new LinkedList<>();
    }

    public WeekdayTemplate() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeekdayTemplate)) return false;

        WeekdayTemplate that = (WeekdayTemplate) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (sortIndex != null ? !sortIndex.equals(that.sortIndex) : that.sortIndex != null) return false;
        if (children != null ? !children.equals(that.children) : that.children != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (sortIndex != null ? sortIndex.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WeekdayTemplate{");
        sb.append("name='").append(name).append('\'');
        sb.append(", sortIndex=").append(sortIndex);
        sb.append(", children=").append(children);
        sb.append('}');
        return sb.toString();
    }

    /**
     * weekday generation method
     */
    public static WeekdayTemplate createWeekdayFromSortIndex(int sortIndex) {
        String name;

        switch (sortIndex) {
            case 1:
                name = "Monday";
                break;
            case 2:
                name = "Tuesday";
                break;
            case 3:
                name = "Wednesday";
                break;
            case 4:
                name = "Thursday";
                break;
            case 5:
                name = "Friday";
                break;
            case 6:
                name = "Saturday";
                break;
            case 0:
                name = "Sunday";
                break;
            default:
                throw new IllegalArgumentException("sortIndex must be >=0 and <=6");
        }

        return new WeekdayTemplate(name, sortIndex);
    }

    @Override
    public int compareTo(WeekdayTemplate o) {
        return sortIndex.compareTo(o.sortIndex);
    }
}
