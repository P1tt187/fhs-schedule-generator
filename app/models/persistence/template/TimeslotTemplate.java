package models.persistence.template;

import models.persistence.AbstractEntity;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Table(name = "TBLTIMESLOT_TEMPLATE")
public class TimeslotTemplate extends AbstractEntity implements Comparable<TimeslotTemplate>{

    @ManyToOne
    private WeekdayTemplate parent;

    @Constraints.Required
    @Constraints.Max(value = 23)
    @Constraints.Min(value = 0)
    @Column(name = "STARTHOUR", nullable = false)
    private Integer startHour;

    @Constraints.Max(value = 59)
    @Constraints.Min(value = 0)
    @Constraints.Required
    @Column(name = "STARTMINUTE", nullable = false)
    private Integer startMinute;

    @Constraints.Max(value = 23)
    @Constraints.Min(value = 0)
    @Constraints.Required
    @Column(name = "STOPHOUR", nullable = false)
    private Integer stopHour;

    @Constraints.Max(value = 59)
    @Constraints.Min(value = 0)
    @Constraints.Required
    @Column(name = "STOPMINUTE", nullable = false)
    private Integer stopMinute;

    public TimeslotTemplate() {

    }

    public TimeslotTemplate(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute, WeekdayTemplate parent) {
        this.parent = parent;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.stopHour = stopHour;
        this.stopMinute = stopMinute;
    }

    public WeekdayTemplate getParent() {
        return parent;
    }

    public void setParent(WeekdayTemplate weekdayTemplate) {
        this.parent = weekdayTemplate;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(Integer startMinute) {
        this.startMinute = startMinute;
    }

    public Integer getStopHour() {
        return stopHour;
    }

    public void setStopHour(Integer stopHour) {
        this.stopHour = stopHour;
    }

    public Integer getStopMinute() {
        return stopMinute;
    }

    public void setStopMinute(Integer stopMinute) {
        this.stopMinute = stopMinute;
    }

    @Override
    public String toString() {
        return "TimeslotTemplate{" +
                "parent=" + parent +
                ", startHour=" + startHour +
                ", startMinute=" + startMinute +
                ", stopHour=" + stopHour +
                ", stopMinute=" + stopMinute +
                '}';
    }

    @Override
    public int compareTo(TimeslotTemplate that) {
        if (that == null) {
            return -1;
        }

        WeekdayTemplate thisWeekday = (WeekdayTemplate) parent;
        WeekdayTemplate thatWeekday = (WeekdayTemplate) that.parent;

        int ret = thisWeekday.compareTo(thatWeekday);
        if (ret != 0) {
            return ret;
        }
        ret = this.startHour.compareTo(that.startHour);
        if (ret != 0) {
            return ret;
        }

        ret = this.startMinute.compareTo(that.startMinute);
        if (ret != 0) {
            return ret;
        }
        ret = this.stopHour.compareTo(that.stopHour);
        if (ret != 0) {
            return ret;

        }


        return this.stopMinute.compareTo(that.stopMinute);

    }
}
