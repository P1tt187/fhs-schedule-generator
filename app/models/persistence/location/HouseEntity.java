package models.persistence.location;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import models.persistence.AbstractEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 10.02.14.
 */
@Entity
@Table(name = "TBLHOUSE")
public class HouseEntity extends AbstractEntity {

    @Column(name = "NAME")
    private String name;

    @JsonManagedReference("rooms")
    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private List<RoomEntity> rooms;

    public HouseEntity() {

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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HouseEntity{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HouseEntity that = (HouseEntity) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        //if (rooms != null ? !(rooms.containsAll(that.rooms) && rooms.size() == that.rooms.size()) : that.rooms != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        //result = 31 * result + (rooms != null ? rooms.hashCode() : 0);
        return result;
    }
}
