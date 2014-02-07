package models.persistence.subject;

import models.persistence.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name="TBLSUBJECT")
public abstract class AbstractSubject extends AbstractEntity {

    private Integer units;

    private String name;


}
