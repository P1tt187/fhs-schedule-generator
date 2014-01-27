package models.persistence;

import javax.persistence.Entity;
import javax.persistence.Table;
import play.db.ebean.Model.Finder;

/**
 * Created by fabian on 27.01.14.
 */
@Entity
@Table(name = "TBLTIMESLOT")
public class Timeslot extends AbstractTimeSlot {


    public static Finder<Long,Timeslot> find = new Finder<>(Long.class,Timeslot.class);
}
