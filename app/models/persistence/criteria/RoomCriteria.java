package models.persistence.criteria;

import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by fabian on 04.02.14.
 */
@Entity
@DiscriminatorValue("ROOMCRIT")
public class RoomCriteria extends AbstractCriteria {
    /**
     * how many people can be in the room
     */
    @Column(name = "CAPACITY", nullable = false)
    @Constraints.Required
    public Integer capacity;

    /**
     * in wich house is the room
     */
    @Column(name = "HOUSE")
    @Constraints.Required
    public String house;

    /**
     * number of room in house
     */
    @Column(name = "NUMBER", nullable = false)
    @Constraints.Required
    public Integer number;


    /**
     * Room is pc pool
     */
    @Column(name = "PCPOOL", nullable = false)
    @Constraints.Required
    public Boolean pcPool;

    /**
     * Room has beamer
     */
    @Column(name = "BEAMER", nullable = false)
    @Constraints.Required
    public Boolean beamer;
}
