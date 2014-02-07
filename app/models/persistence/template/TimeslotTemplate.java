package models.persistence.template;

import models.persistence.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Table(name = "TBLTIMESLOT_TEMPLATE")
public class TimeslotTemplate extends AbstractEntity {

    @ManyToOne
    private WeekdayTemplate parent;

    @Column(name = "STARTHOUR", nullable = false)
    private Integer startHour;
    @Column(name = "STARTMINUTE", nullable = false)
    private Integer startMinute;


    @Column(name = "STOPHOUR", nullable = false)
    private Integer stopHour;

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
}
