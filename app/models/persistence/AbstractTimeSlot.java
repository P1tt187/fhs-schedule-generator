package models.persistence;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;


import play.data.validation.Validation;

import play.db.ebean.Model;

/**
 * Created by fabian on 27.01.14.
 */

@Entity
public abstract class AbstractTimeSlot extends Node {

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
