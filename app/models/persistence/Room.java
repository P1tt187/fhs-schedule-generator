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
    /**database id*/
    public Long id;

    /**how many people can be in the room*/
    @Column(name="CAPACITY", nullable = false)
    @Constraints.Required
    public Integer capacity;

    /**in wich house is the room*/
    @Column(name="HOUSE")
    @Constraints.Required
    public String house;

    /**number of room in house*/
    @Column(name="NUMBER", nullable = false)
    @Constraints.Required
    public Integer number;

    @Column(name="TOLERANCE", nullable = false)
    @Constraints.Required
    public Boolean tolerance;

    /**Room is pc pool*/
    @Column(name="PCPOOL", nullable = false)
    @Constraints.Required
    public Boolean pcPool;

    /**Room has beamer*/
    @Column(name="BEAMER", nullable = false)
    @Constraints.Required
    public Boolean beamer;

}
