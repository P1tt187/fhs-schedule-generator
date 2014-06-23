package models.persistence.location;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author fabian on 09.06.14.
 */
@Embeddable
public class LectureRoom {

    @Column(name = "HOUSE")
    private String house;

    @Column(name = "NUMBER")
    private String number;

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LectureRoom that = (LectureRoom) o;

        if (house != null ? !house.equals(that.house) : that.house != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = house != null ? house.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }

    public int compareTo(RoomEntity roomEntity) {
        int ret = house.compareTo(roomEntity.getHouse().getName());
        if (ret != 0) {
            return ret;
        }
        return number.compareTo(roomEntity.getNumber());
    }

    public int compareTo(LectureRoom that) {
        int ret = this.house.compareTo(that.house);
        if (ret != 0) {
            return ret;
        }
        return this.number.compareTo(that.number);
    }
}
