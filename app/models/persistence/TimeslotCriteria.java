package models.persistence;

import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@DiscriminatorValue("CRITSLOT")
public class TimeslotCriteria extends AbstractTimeSlot {
    @Constraints.Required
    @Column(name = "TOLERANCE")
    public Boolean tolerance;
}
