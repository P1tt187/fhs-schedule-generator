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
/**A weekday is a Node, containing a list of Timeslots*/
public class Weekday extends Node implements Comparable<Weekday> {

    @Column(name = "NAME")
    @Constraints.Required
    public String name;

    @Column(name = "SORTINDEX")
    @Constraints.Required
    public Integer sortIndex;

    @Override
    public String toString() {
        return "Weekday{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(Weekday that) {
        if (that == null) throw new IllegalArgumentException("that must not be null");
        return sortIndex.compareTo(that.sortIndex);
    }
}
