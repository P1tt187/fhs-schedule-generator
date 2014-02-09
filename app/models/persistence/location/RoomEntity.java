package models.persistence.location;

import models.persistence.AbstractEntity;
import models.persistence.criteria.CriteriaContainer;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * Created by fabian on 31.01.14.
 */
@Entity
@Table(name = "TBLROOM")
public class RoomEntity extends AbstractEntity {

    /**
     * how many people can be in the room
     */
    @Constraints.Required
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
    @Constraints.Required
    @Column(name = "NUMBER", nullable = false)
    private Integer number;


    /**
     * RoomEntity is pc pool
     */
    @Column(name = "PCPOOL", nullable = false)
    private Boolean pcPool;

    /**
     * RoomEntity has beamer
     */
    @Column(name = "BEAMER", nullable = false)
    private Boolean beamer;

    /**
     * contains all criterias
     */
    //@JoinTable(name = "fk_critcontainer")
    @OneToOne(cascade = CascadeType.ALL, targetEntity = CriteriaContainer.class)
    private CriteriaContainer criteriaContainer;

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

    public Boolean getPcPool() {
        return pcPool;
    }

    public void setPcPool(Boolean pcPool) {
        this.pcPool = pcPool;
    }

    public Boolean getBeamer() {
        return beamer;
    }

    public void setBeamer(Boolean beamer) {
        this.beamer = beamer;
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
        this(null, null, null, null, null);
    }

    /**
     * parameter constructor
     */
    public RoomEntity(Integer capacity, String house, Integer number, Boolean pcPool, Boolean beamer) {
        this.capacity = capacity;
        this.house = house;
        this.number = number;
        this.pcPool = pcPool;
        this.beamer = beamer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RoomEntity roomEntity = (RoomEntity) o;

        if (beamer != null ? !beamer.equals(roomEntity.beamer) : roomEntity.beamer != null) return false;
        if (capacity != null ? !capacity.equals(roomEntity.capacity) : roomEntity.capacity != null) return false;
        if (house != null ? !house.equals(roomEntity.house) : roomEntity.house != null) return false;
        if (number != null ? !number.equals(roomEntity.number) : roomEntity.number != null) return false;
        if (pcPool != null ? !pcPool.equals(roomEntity.pcPool) : roomEntity.pcPool != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (capacity != null ? capacity.hashCode() : 0);
        result = 31 * result + (house != null ? house.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (pcPool != null ? pcPool.hashCode() : 0);
        result = 31 * result + (beamer != null ? beamer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RoomEntity{");
        sb.append("capacity=").append(capacity);
        sb.append(", id='").append(getId()).append('\'');
        sb.append(", house='").append(house).append('\'');
        sb.append(", number=").append(number);
        sb.append(", pcPool=").append(pcPool);
        sb.append(", beamer=").append(beamer);
        sb.append('}');
        return sb.toString();
    }
}
