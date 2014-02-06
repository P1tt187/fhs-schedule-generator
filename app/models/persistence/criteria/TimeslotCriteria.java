package models.persistence.criteria;


import models.persistence.scheduletree.Weekday;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@DiscriminatorValue("TSLOTCRIT")
public class TimeslotCriteria extends AbstractCriteria {


    public TimeslotCriteria(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute, Weekday weekday) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.stopHour = stopHour;
        this.stopMinute = stopMinute;
        this.weekday = weekday;
    }

    public TimeslotCriteria() {

    }

    @Column(name = "STARTHOUR", nullable = false)
    @Constraints.Required
    public Integer startHour;
    @Column(name = "STARTMINUTE", nullable = false)
    @Constraints.Required
    public Integer startMinute;

    @Constraints.Required
    @Column(name = "STOPHOUR", nullable = false)
    public Integer stopHour;
    @Constraints.Required
    @Column(name = "STOPMINUTE", nullable = false)
    public Integer stopMinute;

    @JoinColumn(name = "fk_weekday")
    @Constraints.Required
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    public Weekday weekday;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimeslotCriteria that = (TimeslotCriteria) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (startHour != null ? !startHour.equals(that.startHour) : that.startHour != null) return false;
        if (startMinute != null ? !startMinute.equals(that.startMinute) : that.startMinute != null) return false;
        if (stopHour != null ? !stopHour.equals(that.stopHour) : that.stopHour != null) return false;
        if (stopMinute != null ? !stopMinute.equals(that.stopMinute) : that.stopMinute != null) return false;
        if (tolerance != null ? !tolerance.equals(that.tolerance) : that.tolerance != null) return false;
        if (weekday != null ? !weekday.equals(that.weekday) : that.weekday != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (tolerance != null ? tolerance.hashCode() : 0);
        result = 31 * result + (startHour != null ? startHour.hashCode() : 0);
        result = 31 * result + (startMinute != null ? startMinute.hashCode() : 0);
        result = 31 * result + (stopHour != null ? stopHour.hashCode() : 0);
        result = 31 * result + (stopMinute != null ? stopMinute.hashCode() : 0);
        result = 31 * result + (weekday != null ? weekday.hashCode() : 0);
        return result;
    }
}
