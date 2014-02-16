package models.persistence.participants;

import models.persistence.AbstractEntity;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLPARTICIPANTS")
public abstract class Participant extends AbstractEntity {


    @Column(name = "SIZE", nullable = false)
    protected Integer size;

    @ManyToMany(mappedBy = "participants")
    protected List<Student> students;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
