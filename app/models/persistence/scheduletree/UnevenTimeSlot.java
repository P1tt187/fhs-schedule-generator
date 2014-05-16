package models.persistence.scheduletree;

import models.persistence.enumerations.EDuration;

import javax.persistence.*;

/**
 * @author fabian
 *         on 28.04.14.
 */
@Entity
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("UT")
@Table(name="TBLTIMESLOT_UNEVEN")
public class UnevenTimeSlot extends TimeSlot {
    public UnevenTimeSlot() {
        super();
    }

    @Override
    public EDuration getDuration() {
        return EDuration.UNEVEN;
    }

    public UnevenTimeSlot(Integer startHour, Integer startMinute, Integer stopHour, Integer stopMinute, Boolean unpopular) {
        super(startHour, startMinute, stopHour, stopMinute, unpopular);
    }
}
