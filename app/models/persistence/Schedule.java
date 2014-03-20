package models.persistence;

import models.persistence.scheduletree.Root;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author fabian
 *         on 20.03.14.
 */
@Entity
@Table(name="TBLSCHEDULE")
public class Schedule extends AbstractEntity {

    @OneToOne(targetEntity = Root.class)
    private Root root;

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        if (root != null ? !root.equals(schedule.root) : schedule.root != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return root != null ? root.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "root=" + root +
                '}';
    }
}
