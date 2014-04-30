package models.persistence.criteria;

import models.persistence.enumerations.EDocentTimeKind;
import models.persistence.enumerations.EDuration;
import models.persistence.template.WeekdayTemplate;

import javax.persistence.*;

/**
 * @author fabian
 *         on 30.04.14.
 */
@Entity
@Table(name = "TBLDOCENT_TIME_WISH")
public class DocentTimeWish extends TimeSlotCriteria {

    @Column(name = "TIMEKIND")
    @Enumerated(EnumType.STRING)
    private EDocentTimeKind timeKind;

    public EDocentTimeKind getTimeKind() {
        return timeKind;
    }

    public void setTimeKind(EDocentTimeKind timeKind) {
        this.timeKind = timeKind;
    }

    public DocentTimeWish(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute, WeekdayTemplate weekday, EDuration duration, EDocentTimeKind timeKind) {
        super(startHour, startMinute, stopHour, stopMinute, weekday, duration);
        this.timeKind = timeKind;
    }

    public DocentTimeWish() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DocentTimeWish that = (DocentTimeWish) o;

        if (timeKind != that.timeKind) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (timeKind != null ? timeKind.hashCode() : 0);
        return result;
    }
}
