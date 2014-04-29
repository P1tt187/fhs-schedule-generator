package models.persistence.scheduletree;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import models.persistence.criteria.TimeSlotCriteria;
import models.persistence.enumerations.EDuration;
import models.persistence.lecture.AbstractLecture;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

/**
 * @author fabian
 *         on 27.01.14.
 */
@Entity
@Table(name = "TBLTIMESLOT")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "SLOT_TYPE")
@DiscriminatorValue("T")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
/** A timeslot is a Node*/
public abstract class TimeSlot extends Node implements Comparable<TimeSlot> {

    public TimeSlot(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.stopHour = stopHour;
        this.stopMinute = stopMinute;
    }

    public TimeSlot() {

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

        TimeSlot timeSlot = (TimeSlot) o;

        if (startHour != null ? !startHour.equals(timeSlot.startHour) : timeSlot.startHour != null) return false;
        if (startMinute != null ? !startMinute.equals(timeSlot.startMinute) : timeSlot.startMinute != null)
            return false;
        if (stopHour != null ? !stopHour.equals(timeSlot.stopHour) : timeSlot.stopHour != null) return false;
        if (stopMinute != null ? !stopMinute.equals(timeSlot.stopMinute) : timeSlot.stopMinute != null) return false;
        if (parent != null) {
            Weekday weekday = (Weekday) parent;
            Weekday timeSlotWeekday = (Weekday) timeSlot.parent;
            if (weekday.getSortIndex() != timeSlotWeekday.getSortIndex()) return false;
        }
        return this.getDuration() == timeSlot.getDuration();

    }

    @JsonIgnore
    public Boolean isInTimeSlotCriteria(TimeSlotCriteria timeSlotCriteria) {

        int otherStartHour = timeSlotCriteria.getStartHour();
        int otherStartMinute = timeSlotCriteria.getStartMinute();
        int otherStopHour = timeSlotCriteria.getStopHour();
        int otherStopMinute = timeSlotCriteria.getStopMinute();
        int thisWeekday = ((Weekday) parent).getSortIndex();
        int otherWeekday = timeSlotCriteria.getWeekday().getSortIndex();

        if (thisWeekday != otherWeekday) {
            return false;
        }

        Calendar thisStartDate = Calendar.getInstance();
        Calendar thisStopDate = Calendar.getInstance();
        Calendar thatStartDate = Calendar.getInstance();
        Calendar thatStopDate = Calendar.getInstance();

        initCalendarFields(thisStartDate, startHour, startMinute, thisWeekday);
        initCalendarFields(thisStopDate, stopHour, stopMinute, thisWeekday);
        initCalendarFields(thatStartDate, otherStartHour, otherStartMinute, otherWeekday);
        initCalendarFields(thatStopDate, otherStopHour, otherStopMinute, otherWeekday);

        return thisStartDate.compareTo(thatStartDate) >= 0 && thisStopDate.compareTo(thatStopDate) <= 0
                && (timeSlotCriteria.getDuration() == EDuration.WEEKLY || timeSlotCriteria.getDuration() == getDuration());
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
        if (parent != null) {
            Weekday weekday = (Weekday) parent;
            result = 31 * result + weekday.getSortIndex();
        }

        result = 31 * result + getDuration().hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TimeSlot{");
        sb.append("startHour=").append(startHour);
        sb.append(", startMinute=").append(startMinute);
        sb.append(", stopHour=").append(stopHour);
        sb.append(", stopMinute=").append(stopMinute);
        sb.append(", Weekday=").append(((Weekday) parent).getName());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(TimeSlot that) {

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

    @JsonIgnore
    public abstract EDuration getDuration();
}
