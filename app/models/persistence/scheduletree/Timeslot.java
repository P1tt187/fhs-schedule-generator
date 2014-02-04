package models.persistence.scheduletree;

import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by fabian on 27.01.14.
 */
@Entity
@DiscriminatorValue("TIMESLOT")
/** A timeslot is a Node*/
public class Timeslot extends Node implements Comparable<Timeslot> {


    @Column(name = "STARTHOUR")
    @Constraints.Required
    public Integer startHour;
    @Column(name = "STARTMINUTE")
    @Constraints.Required
    public Integer startMinute;

    @Constraints.Required
    @Column(name = "STOPHOUR")
    public Integer stopHour;
    @Constraints.Required
    @Column(name = "STOPMINUTE")
    public Integer stopMinute;

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
