package models.persistence.location;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
public class RoomEntity extends AbstractEntity implements Comparable<RoomEntity> {

    /**
     * how many people can be in the room
     */

    @Column(name = "CAPACITY", nullable = false)
    private Integer capacity;


    /**
     * number of room in house
     */

    @Column(name = "NUMBER", nullable = false)
    private String number;

    /**
     * the room is in a house
     */
    @ManyToOne
    @OrderColumn(name = "name")
    @JsonBackReference("rooms")
    private HouseEntity house;

    public HouseEntity getHouse() {
        return house;
    }

    public void setHouse(HouseEntity house) {
        this.house = house;
    }


    /**
     * Attributes of this room
     */
    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(targetEntity = RoomAttributesEntity.class, cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@roomAttributesId")
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

    @ManyToOne(cascade = CascadeType.ALL, targetEntity = CriteriaContainer.class)
    private CriteriaContainer criteriaContainer;

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
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
    public RoomEntity(Integer capacity, String number, HouseEntity house) {
        this.capacity = capacity;
        this.number = number;
        this.house = house;

    }


    public LectureRoom roomEntity2LectureRoom(){
        LectureRoom lectureRoom = new LectureRoom();
        lectureRoom.setHouse(this.house.getName());
        lectureRoom.setNumber(this.number);
        return lectureRoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomEntity)) return false;

        RoomEntity that = (RoomEntity) o;

        if (capacity != null ? !capacity.equals(that.capacity) : that.capacity != null) return false;

        if (house != null ? !house.equals(that.house) : that.house != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (roomAttributes != null ? !(roomAttributes.containsAll(that.roomAttributes) && this.roomAttributes.size() == that.roomAttributes.size() ) : that.roomAttributes != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = capacity != null ? capacity.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (house != null ? house.hashCode() : 0);
        result = 31 * result + (roomAttributes != null ? roomAttributes.stream().mapToInt(RoomAttributesEntity::hashCode).sum() : 0);
        result = 31 * result + (criteriaContainer != null ? criteriaContainer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RoomEntity{");
        sb.append("capacity=").append(capacity);
        sb.append(", number='").append(number).append('\'');
        sb.append(", house=").append(house);
        sb.append(", roomAttributes=").append(roomAttributes);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(RoomEntity that) {

        int ret = 0;
        ret += this.house.getName().compareTo(that.house.getName());
        if (ret != 0) {
            return ret;
        }
        ret += this.number.compareTo(that.number);
        if (ret != 0) {
            return ret;
        }

        ret += this.capacity.compareTo(that.capacity);

        return ret;
    }
}
