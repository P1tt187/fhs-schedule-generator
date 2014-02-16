package models.persistence.subject;

import models.persistence.AbstractEntity;

import javax.persistence.*;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name="TBLSUBJECT")
public abstract class AbstractSubject extends AbstractEntity {

    @Column(name="UNITS")
    private Integer units;

    @Column(name="NAME")
    private String name;

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
