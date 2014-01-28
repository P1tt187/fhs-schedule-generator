package models.persistence;

import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLLECTURE")
public class Lecture extends AbstractLecture {

    @Column(name = "NAME")
    @Constraints.Required
    public String name;
}
