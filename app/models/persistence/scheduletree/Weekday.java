package models.persistence.scheduletree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.LinkedList;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLWEEKDAY")
/**A weekday is a Node, containing a list of Timeslots*/
public class Weekday extends Node implements Comparable<Weekday> {

    @Column(name = "NAME")
    private String name;

    @Column(name = "SORTINDEX")
    private Integer sortIndex;

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



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }
}
