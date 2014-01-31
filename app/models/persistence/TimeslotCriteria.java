package models.persistence;


import models.persistence.scheduletree.Weekday;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLTIMESLOTCRITERIA")
public class TimeslotCriteria extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    public Long id;

    @Constraints.Required
    @Column(name = "TOLERANCE", nullable = false)
    public Boolean tolerance;

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
}
