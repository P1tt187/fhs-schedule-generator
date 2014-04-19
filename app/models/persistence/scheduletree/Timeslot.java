package models.persistence.scheduletree;


import com.fasterxml.jackson.annotation.JsonIgnore;
import models.persistence.criteria.TimeslotCriteria;
import models.persistence.lecture.AbstractLecture;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

/**
 * Created by fabian on 27.01.14.
 */
@Entity
@Table(name = "TBLTIMESLOT")
/** A timeslot is a Node*/
public class Timeslot extends Node implements Comparable<Timeslot> {

    public Timeslot(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.stopHour = stopHour;
        this.stopMinute = stopMinute;
    }

    public Timeslot() {

    }


    @Column(name = "STARTHOUR", nullable = false)
    private Integer startHour;


    @Column(name = "STARTMINUTE", nullable = false)
    private Integer startMinute;


    @Column(name = "STOPHOUR", nullable = false)
    private Integer stopHour;


    @Column(name = "STOPMINUTE", nullable = false)
    private Integer stopMinute;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = AbstractLecture.class)
    private List<AbstractLecture> lectures;

    public List<AbstractLecture> getLectures() {
        return lectures;
    }

    public void setLectures(List<AbstractLecture> lectures) {
        this.lectures = lectures;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Timeslot timeslot = (Timeslot) o;

        if (startHour != null ? !startHour.equals(timeslot.startHour) : timeslot.startHour != null) return false;
        if (startMinute != null ? !startMinute.equals(timeslot.startMinute) : timeslot.startMinute != null)
            return false;
        if (stopHour != null ? !stopHour.equals(timeslot.stopHour) : timeslot.stopHour != null) return false;
        if (stopMinute != null ? !stopMinute.equals(timeslot.stopMinute) : timeslot.stopMinute != null) return false;

        return true;
    }

    @JsonIgnore
    public Boolean isInTimeslotCriteria(TimeslotCriteria timeslotCriteria) {

        int otherStartHour = timeslotCriteria.getStartHour();
        int otherStartMinute = timeslotCriteria.getStartMinute();
        int otherStopHour = timeslotCriteria.getStopHour();
        int otherStopMinute = timeslotCriteria.getStopMinute();
        int thisWeekday = ((Weekday) parent).getSortIndex();
        int otherWeekday = timeslotCriteria.getWeekday().getSortIndex();

        Calendar thisStartDate = Calendar.getInstance();
        Calendar thisStopDate = Calendar.getInstance();
        Calendar thatStartDate = Calendar.getInstance();
        Calendar thatStopDate = Calendar.getInstance();

        initCalendarFields(thisStartDate, startHour, startMinute, thisWeekday);
        initCalendarFields(thisStopDate, stopHour, stopMinute, thisWeekday);
        initCalendarFields(thatStartDate, otherStartHour, otherStartMinute, otherWeekday);
        initCalendarFields(thatStopDate, otherStopHour, otherStopMinute, otherWeekday);

        return thisStartDate.compareTo(thatStartDate) >= 0 &&  thatStopDate.compareTo(thisStopDate) <= 0;
    }

    private void initCalendarFields(Calendar calendar, int hour, int minute, int weekdayIndex) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, weekdayIndex + 1);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (startHour != null ? startHour.hashCode() : 0);
        result = 31 * result + (startMinute != null ? startMinute.hashCode() : 0);
        result = 31 * result + (stopHour != null ? stopHour.hashCode() : 0);
        result = 31 * result + (stopMinute != null ? stopMinute.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Timeslot{" +
                "startHour=" + startHour +
                ", startMinute=" + startMinute +
                ", stopHour=" + stopHour +
                ", stopMinute=" + stopMinute +
                ", weekday=" + parent +
                '}';
    }

    @Override
    public int compareTo(Timeslot that) {

        if (that == null) {
            return -1;
        }

        Weekday thisWeekday = (Weekday) parent;
        Weekday thatWeekday = (Weekday) that.parent;

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
