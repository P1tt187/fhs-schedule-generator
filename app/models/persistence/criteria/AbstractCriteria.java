package models.persistence.criteria;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import models.persistence.AbstractEntity;
import models.persistence.enumerations.EPriority;

import javax.persistence.*;

/**
 * Created by fabian on 04.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLCRITERIA")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractCriteria extends AbstractEntity implements Comparable<AbstractCriteria> {

    /**
     * is the system allowed to use other possibilitys
     */
    @Column(name = "TOLERANCE", nullable = false)
    private Boolean tolerance;

    /**
     * priority of this criteria
     */
    @Column(name = "PRIORITY", nullable = false)
    @Enumerated(EnumType.STRING)
    private EPriority priority;

    public Boolean isTolerance() {
        return tolerance;
    }

    public void setTolerance(Boolean tolerance) {
        this.tolerance = tolerance;
    }

    public EPriority getPriority() {
        return priority;
    }

    public void setPriority(EPriority priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(AbstractCriteria that) {
        return this.priority.getSortIndex().compareTo(that.priority.getSortIndex());
    }
}
