package models.persistence;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by fabian on 31.01.14.
 */
@Entity
@Table(name="TBLROOM")
public class Room extends Model {

    @Id
    @Column(name="ID", nullable = false)
    @Constraints.Required
    public Long id;

    @Column(name="CAPACITY", nullable = false)
    @Constraints.Required
    public Integer capacity;

    @Column(name="NUMBER", nullable = false)
    @Constraints.Required
    public Integer number;

    @Column(name="TOLERANCE", nullable = false)
    @Constraints.Required
    public Boolean tolerance;

    @Column(name="PCPOOL", nullable = false)
    @Constraints.Required
    public Boolean pcPool;

    @Column(name="BEAMER", nullable = false)
    @Constraints.Required
    public Boolean beamer;

}
