package models.persistence.location;

import models.persistence.AbstractEntity;
import models.persistence.criteria.CriteriaContainer;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 31.01.14.
 */
@Entity
@Table(name = "TBLROOM")
public class RoomEntity extends AbstractEntity {

    /**
     * how many people can be in the room
     */

    @Column(name = "CAPACITY", nullable = false)
    private Integer capacity;


    /**
     * number of room in house
     */

    @Column(name = "NUMBER", nullable = false)
    private Integer number;

    @ManyToOne
    @OrderColumn(name="name")
    private HouseEntity house;

    public HouseEntity getHouse() {
        return house;
    }

    public void setHouse(HouseEntity house) {
        this.house = house;
    }


    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = RoomAttributesEntity.class,cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RoomAttributesEntity> roomAttributes;

    public List<RoomAttributesEntity> getRoomAttributes() {
        return roomAttributes;
    }

    public void setRoomAttributes(List<RoomAttributesEntity> roomAttributes) {
        this.roomAttributes = roomAttributes;
    }

    /**
     * contains all criterias
     */

    @OneToOne(cascade = CascadeType.ALL, targetEntity = CriteriaContainer.class)
    private CriteriaContainer criteriaContainer;

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }


    public CriteriaContainer getCriteriaContainer() {
        return criteriaContainer;
    }

    public void setCriteriaContainer(CriteriaContainer criteriaContainer) {
        this.criteriaContainer = criteriaContainer;
    }

    /**
     * default constructor
     */
    public RoomEntity() {
        this(null, null, null);
    }

    /**
     * parameter constructor
     */
    public RoomEntity(Integer capacity, Integer number, HouseEntity house) {
        this.capacity = capacity;
        this.number = number;
        this.house = house;

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomEntity)) return false;

        RoomEntity that = (RoomEntity) o;

        if (capacity != null ? !capacity.equals(that.capacity) : that.capacity != null) return false;
        if (criteriaContainer != null ? !criteriaContainer.equals(that.criteriaContainer) : that.criteriaContainer != null)
            return false;
        if (house != null ? !house.equals(that.house) : that.house != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (roomAttributes != null ? !roomAttributes.equals(that.roomAttributes) : that.roomAttributes != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = capacity != null ? capacity.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (house != null ? house.hashCode() : 0);
        result = 31 * result + (roomAttributes != null ? roomAttributes.hashCode() : 0);
        result = 31 * result + (criteriaContainer != null ? criteriaContainer.hashCode() : 0);
        return result;
    }
}
