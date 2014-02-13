package models.persistence.location;

import models.persistence.AbstractEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 10.02.14.
 */
@Entity
@Table(name="TBLHOUSE")
public class HouseEntity extends AbstractEntity{

    @Column(name="NAME")
    private String name;

    @Transient
    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<RoomEntity> rooms;

    public HouseEntity(){

    }

    public HouseEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoomEntity> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomEntity> rooms) {
        this.rooms = rooms;
    }
}
