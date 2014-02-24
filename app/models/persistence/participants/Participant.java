package models.persistence.participants;

import models.persistence.AbstractEntity;

import javax.persistence.*;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLPARTICIPANTS")
public abstract class Participant extends AbstractEntity {


    @Column(name = "SIZE", nullable = false)
    protected Integer size;


    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
