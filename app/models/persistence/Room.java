package models.persistence;

import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by fabian on 31.01.14.
 */
@Entity
@Table(name = "TBLROOM")
public class Room extends AbstractEntity {

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

    @Column(name = "TOLERANCE", nullable = false)
    @Constraints.Required
    public Boolean tolerance;

    /**
     * Room is pc pool
     */
    @Column(name = "PCPOOL", nullable = false)
    @Constraints.Required
    public Boolean pcPool;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Room room = (Room) o;

        if (beamer != null ? !beamer.equals(room.beamer) : room.beamer != null) return false;
        if (capacity != null ? !capacity.equals(room.capacity) : room.capacity != null) return false;
        if (house != null ? !house.equals(room.house) : room.house != null) return false;
        if (number != null ? !number.equals(room.number) : room.number != null) return false;
        if (pcPool != null ? !pcPool.equals(room.pcPool) : room.pcPool != null) return false;
        if (tolerance != null ? !tolerance.equals(room.tolerance) : room.tolerance != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0);
        result = 31 * result + (house != null ? house.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (tolerance != null ? tolerance.hashCode() : 0);
        result = 31 * result + (pcPool != null ? pcPool.hashCode() : 0);
        result = 31 * result + (beamer != null ? beamer.hashCode() : 0);
        return result;
    }

    /**
     * Room has beamer
     */
    @Column(name = "BEAMER", nullable = false)
    @Constraints.Required
    public Boolean beamer;

}
