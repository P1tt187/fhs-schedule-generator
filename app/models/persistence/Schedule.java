package models.persistence;

import models.persistence.scheduletree.Root;

import javax.persistence.*;

/**
 * @author fabian
 *         on 20.03.14.
 */
@Entity
@Table(name = "TBLSCHEDULE")
public class Schedule extends AbstractEntity {

    @OneToOne(targetEntity = Root.class)
    private Root root;

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

    /**
     * Semester of this Schedule
     */
    @ManyToOne(targetEntity = Semester.class, cascade = CascadeType.MERGE)
    private Semester semester;

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
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
