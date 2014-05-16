package models.persistence.criteria;

import models.persistence.location.HouseEntity;
import models.persistence.location.RoomAttributesEntity;
import models.persistence.location.RoomEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * @author fabian
 *         on 04.02.14.
 */
@Entity
@Table(name = "TBLROOMCRITERIA")
public class RoomCriteria extends AbstractCriteria {

    /**
     * a house can be a criteria
     */
    @ManyToOne(targetEntity = HouseEntity.class)
    private HouseEntity house;

    /**
     * a room can be a criteria
     */
    @ManyToOne(targetEntity = RoomEntity.class)
    private RoomEntity room;

    /**
     * a specific attributes can be a criteria
     */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = RoomAttributesEntity.class,  fetch = FetchType.EAGER)
    private List<RoomAttributesEntity> roomAttributes;

    public List<RoomAttributesEntity> getRoomAttributes() {
        return roomAttributes;
    }

    public void setRoomAttributes(List<RoomAttributesEntity> roomAttributes) {
        this.roomAttributes = roomAttributes;
    }

    public HouseEntity getHouse() {
        return house;
    }

    public void setHouse(HouseEntity house) {
        this.house = house;
    }

    public RoomEntity getRoom() {
        return room;
    }

    public void setRoom(RoomEntity room) {
        this.room = room;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomCriteria that = (RoomCriteria) o;

        if (house != null ? !house.equals(that.house) : that.house != null) return false;
        if (room != null ? !room.equals(that.room) : that.room != null) return false;
        if (roomAttributes != null ? !(roomAttributes.containsAll(that.roomAttributes) && roomAttributes.size() == that.roomAttributes.size()) : that.roomAttributes != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = house != null ? house.hashCode() : 0;
        result = 31 * result + (room != null ? room.hashCode() : 0);
        result = 31 * result + (roomAttributes != null ? roomAttributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RoomCriteria{");
        sb.append("house=").append(house);
        sb.append(", room=").append(room);
        sb.append(", roomAttributes=").append(roomAttributes);
        sb.append('}');
        return sb.toString();
    }
}
