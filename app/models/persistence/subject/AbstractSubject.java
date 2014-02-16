package models.persistence.subject;

import models.persistence.AbstractEntity;
import models.persistence.Docent;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name="TBLSUBJECT")
public abstract class AbstractSubject extends AbstractEntity {

    @Column(name="UNITS")
    private Integer units;

    @Column(name="NAME")
    private String name;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = Docent.class,fetch = FetchType.EAGER)
    private List<Docent> docents;

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Docent> getDocents() {
        return docents;
    }

    public void setDocents(List<Docent> docents) {
        this.docents = docents;
    }
}
