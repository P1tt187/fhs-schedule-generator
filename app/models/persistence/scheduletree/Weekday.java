package models.persistence.scheduletree;

import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@DiscriminatorValue("WEEKDAY")
public class Weekday extends Node{

    @Column(name="NAME")
    @Constraints.Required
    public String name;

    @Column(name="INDEX")
    @Constraints.Required
    public Integer sortIndex;

    @Override
    public String toString() {
        return "Weekday{" +
                "name='" + name + '\'' +
                '}';
    }
}
