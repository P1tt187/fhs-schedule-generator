package models.persistence.scheduletree;

import models.persistence.enumerations.EDuration;

import javax.persistence.*;

/**
 * @author fabian
 *         on 28.04.14.
 */
@Entity
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("ET")
@Table(name="TBLEVEN_TIMESLOT")
public class EvenTimeSlot extends TimeSlot {

    public EvenTimeSlot() {
        super();
    }

    @Override
    public EDuration getDuration() {
        return EDuration.EVEN;
    }

    public EvenTimeSlot(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute) {
        super(startHour, startMinute, stopHour, stopMinute);
    }
}
