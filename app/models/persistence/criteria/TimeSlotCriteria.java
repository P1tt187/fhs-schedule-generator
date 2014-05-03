package models.persistence.criteria;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import models.persistence.enumerations.EDuration;
import models.persistence.template.WeekdayTemplate;

import javax.persistence.*;
import java.util.Calendar;

/**
 * @author fabian
 *         on 28.01.14.
 */
@Entity
@Table(name = "TBLTIMESLOT_CRITERIA")
@Inheritance(strategy = InheritanceType.JOINED)
public class TimeSlotCriteria extends AbstractCriteria {


    public TimeSlotCriteria(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute, WeekdayTemplate weekday, EDuration duration) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.stopHour = stopHour;
        this.stopMinute = stopMinute;
        this.weekday = weekday;
        this.duration = duration;
    }

    public TimeSlotCriteria() {

    }

    @Enumerated(EnumType.STRING)
    @Column(name = "DURATION", nullable = false)
    private EDuration duration;

    @Column(name = "STARTHOUR", nullable = false)
    private Integer startHour;


    @Column(name = "STARTMINUTE", nullable = false)
    private Integer startMinute;


    @Column(name = "STOPHOUR", nullable = false)
    private Integer stopHour;


    @Column(name = "STOPMINUTE", nullable = false)
    private Integer stopMinute;

    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    @ManyToOne(fetch = FetchType.EAGER, optional = false, targetEntity = WeekdayTemplate.class)
    @OrderBy("sortIndex")
    private WeekdayTemplate weekday;


    public EDuration getDuration() {
        return duration;
    }

    public void setDuration(EDuration duration) {
        this.duration = duration;
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

    public WeekdayTemplate getWeekday() {
        return weekday;
    }

    public void setWeekday(WeekdayTemplate weekday) {
        this.weekday = weekday;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimeSlotCriteria that = (TimeSlotCriteria) o;

        if (startHour != null ? !startHour.equals(that.startHour) : that.startHour != null) return false;
        if (startMinute != null ? !startMinute.equals(that.startMinute) : that.startMinute != null) return false;
        if (stopHour != null ? !stopHour.equals(that.stopHour) : that.stopHour != null) return false;
        if (stopMinute != null ? !stopMinute.equals(that.stopMinute) : that.stopMinute != null) return false;
        if (isTolerance() != null ? !isTolerance().equals(that.isTolerance()) : that.isTolerance() != null)
            return false;
        if (weekday != null ? !weekday.getSortIndex().equals(that.weekday.getSortIndex()) : that.weekday != null) return false;
        if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;

        return true;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TimeSlotCriteria{");
        sb.append("duration=").append(duration);
        sb.append(", startHour=").append(startHour);
        sb.append(", startMinute=").append(startMinute);
        sb.append(", stopHour=").append(stopHour);
        sb.append(", stopMinute=").append(stopMinute);
        sb.append(", weekday=").append(weekday);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isTolerance() != null ? isTolerance().hashCode() : 0);
        result = 31 * result + (startHour != null ? startHour.hashCode() : 0);
        result = 31 * result + (startMinute != null ? startMinute.hashCode() : 0);
        result = 31 * result + (stopHour != null ? stopHour.hashCode() : 0);
        result = 31 * result + (stopMinute != null ? stopMinute.hashCode() : 0);
        result = 31 * result + (weekday != null ? weekday.hashCode() : 0);
        result = 37 * result + (duration != null ? duration.hashCode() : 0);
        return result;
    }

    @JsonIgnore
    public Boolean isInTimeSlotCriteria(TimeSlotCriteria timeSlotCriteria) {

        int otherStartHour = timeSlotCriteria.getStartHour();
        int otherStartMinute = timeSlotCriteria.getStartMinute();
        int otherStopHour = timeSlotCriteria.getStopHour();
        int otherStopMinute = timeSlotCriteria.getStopMinute();
        int thisWeekday = weekday.getSortIndex();
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
                && (timeSlotCriteria.duration == EDuration.WEEKLY || timeSlotCriteria.duration == duration);
    }

    private void initCalendarFields(Calendar calendar, int hour, int minute, int weekdayIndex) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, weekdayIndex + 1);
    }
}