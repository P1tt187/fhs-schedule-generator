package models.persistence;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import play.db.ebean.Model.Finder;

/**
 * Created by fabian on 27.01.14.
 */
@Entity
@DiscriminatorValue("TIMESLOT")
public class Timeslot extends AbstractTimeSlot {



}
