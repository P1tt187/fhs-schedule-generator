package models.persistence.criteria;


import models.persistence.AbstractEntity;
import org.hibernate.annotations.DiscriminatorOptions;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * Created by fabian on 04.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLCRITERIA")
@DiscriminatorOptions(force = true)
public abstract class AbstractCriteria extends AbstractEntity {

    /** is the system allowed to use other possibilitys*/
    @Constraints.Required
    @Column(name = "TOLERANCE", nullable = false)
    public Boolean tolerance;

    /** priority of this criteria */
    @Constraints.Required
    @Column(name="PRIORITY")
    public Integer priority;
}
