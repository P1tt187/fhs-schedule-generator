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
public class Timeslot extends Node {


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
}
