package models.persistence.participants;

import models.persistence.AbstractEntity;

import javax.persistence.*;
import java.util.List;

/**
 * @author  fabian
 * on 10.02.14.
 */
@Entity
@Table(name="TBLSTUDENT")
public class Student extends AbstractEntity{

    @Column(name="MATRIKEL",nullable = false)
    private Integer matrikel;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Participant> participants;

    public Integer getMatrikel() {
        return matrikel;
    }

    public void setMatrikel(Integer matrikel) {
        this.matrikel = matrikel;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}
