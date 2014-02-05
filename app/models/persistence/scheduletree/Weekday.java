package models.persistence.scheduletree;

import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.LinkedList;

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

    /** constructor for fields */
    public Weekday(String name, int sortIndex) {
        this.name = name;
        this.sortIndex = sortIndex;
        this.children = new LinkedList<>();
    }

    /**
     * default constructor
     */
    public Weekday() {

    }

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

    /**
     * weekday generation method
     */
    public static Weekday createWeekdayFromSortIndex(int sortIndex) {
        String name;

        switch (sortIndex) {
            case 1:
                name = "Monday";
                break;
            case 2:
                name = "Tuesday";
                break;
            case 3:
                name = "Wednesday";
                break;
            case 4:
                name = "Thursday";
                break;
            case 5:
                name = "Friday";
                break;
            case 6:
                name = "Saturday";
                break;
            case 0:
                name = "Sunday";
                break;
            default:
                throw new IllegalArgumentException("sortIndex must be >=0 and <=6");
        }

        return new Weekday(name, sortIndex);
    }
}
