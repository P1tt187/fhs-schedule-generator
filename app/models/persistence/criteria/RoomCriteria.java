package models.persistence.criteria;

import models.persistence.location.RoomAttributesEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 04.02.14.
 */
@Entity
@Table(name = "TBLROOMCRITERIA")
public class RoomCriteria extends AbstractCriteria {
    /**
     * how many people can be in the room
     */

    @Column(name = "CAPACITY", nullable = false)
    private Integer capacity;

    /**
     * in wich house is the room
     */
    @Column(name = "HOUSE")
    private String house;

    /**
     * number of room in house
     */

    @Column(name = "NUMBER", nullable = false)
    private Integer number;


    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = RoomAttributesEntity.class,cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RoomAttributesEntity> roomAttributes;

    public List<RoomAttributesEntity> getRoomAttributes() {
        return roomAttributes;
    }

    public void setRoomAttributes(List<RoomAttributesEntity> roomAttributes) {
        this.roomAttributes = roomAttributes;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }


}
