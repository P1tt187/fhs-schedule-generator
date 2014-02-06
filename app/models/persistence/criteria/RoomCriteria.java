package models.persistence.criteria;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by fabian on 04.02.14.
 */
@Entity
@Table(name="TBLROOMCRITERIA")
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


    /**
     * Room is pc pool
     */
    @Column(name = "PCPOOL", nullable = false)
    private Boolean pcPool;

    /**
     * Room has beamer
     */
    @Column(name = "BEAMER", nullable = false)
    private Boolean beamer;

    public Boolean getBeamer() {
        return beamer;
    }

    public void setBeamer(Boolean beamer) {
        this.beamer = beamer;
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

    public Boolean getPcPool() {
        return pcPool;
    }

    public void setPcPool(Boolean pcPool) {
        this.pcPool = pcPool;
    }


}
