package models.persistence.location;

import models.persistence.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author  fabian
 * on 10.02.14.
 */
@Entity
@Table(name = "TBLROOM_ATTRIBUTES")
public class RoomAttributesEntity extends AbstractEntity {


    @Column(name = "ATTRIBUTE")
    private String attribute;

    public RoomAttributesEntity(String attribute) {
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomAttributesEntity)) return false;

        RoomAttributesEntity that = (RoomAttributesEntity) o;

        if (attribute != null ? !attribute.equals(that.attribute) : that.attribute != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "RoomAttributesEntity{" + "attribute='" + attribute + '}' + '\'';
    }

    @Override
    public int hashCode() {
        return attribute != null ? attribute.hashCode() : 0;
    }

    /**
     * default constructor
     */
    public RoomAttributesEntity() {

    }


}
