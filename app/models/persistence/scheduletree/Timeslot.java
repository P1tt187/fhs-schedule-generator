package models.persistence.scheduletree;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by fabian on 27.01.14.
 */
@Entity
@DiscriminatorValue("TIMESLOT")
public class Timeslot extends AbstractTimeSlot {



}
