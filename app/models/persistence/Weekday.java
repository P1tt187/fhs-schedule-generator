package models.persistence;

import play.data.validation.Constraints;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@DiscriminatorValue("WEEKDAY")
public class Weekday extends Node{
    @Constraints.Required
    public String name;

    @Constraints.Required
    public Integer index;

    @Override
    public String toString() {
        return "Weekday{" +
                "name='" + name + '\'' +
                '}';
    }
}
