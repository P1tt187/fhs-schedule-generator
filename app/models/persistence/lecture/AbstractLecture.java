package models.persistence.lecture;

import models.persistence.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="TBLALECTURE")
public abstract class AbstractLecture extends AbstractEntity {


}
