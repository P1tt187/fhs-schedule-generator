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
@Table(name = "TBLTIMESLOT_EVEN")
public class EvenTimeSlot extends TimeSlot {

    public EvenTimeSlot() {
        super();
    }

    @Override
    public EDuration getDuration() {
        return EDuration.EVEN;
    }

    public EvenTimeSlot(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute, Boolean unpopular) {
        super(startHour, startMinute, stopHour, stopMinute, unpopular);
    }
}
