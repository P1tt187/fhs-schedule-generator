package models.persistence.location;

import models.persistence.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by fabian on 10.02.14.
 */
@Entity
@Table(name = "TBLROOM_ATTRIBUTES")
public class RoomAttributesEntity extends AbstractEntity {

    @Column(name = "BEAMER")
    private Boolean beamer;

    @Column(name = "PCPOOL")
    private Boolean pcpool;

    @Column(name = "WHITEBOARD")
    private Boolean whiteboard;

    @Column(name = "OVERHEAD")
    private Boolean overhead;

    /**default constructor*/
    public RoomAttributesEntity(){

    }

    public RoomAttributesEntity(Boolean beamer, Boolean pcpool, Boolean whiteboard, Boolean overhead) {
        this.beamer = beamer;
        this.pcpool = pcpool;
        this.whiteboard = whiteboard;
        this.overhead = overhead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomAttributesEntity)) return false;

        RoomAttributesEntity that = (RoomAttributesEntity) o;

        if (beamer != null ? !beamer.equals(that.beamer) : that.beamer != null) return false;
        if (overhead != null ? !overhead.equals(that.overhead) : that.overhead != null) return false;
        if (pcpool != null ? !pcpool.equals(that.pcpool) : that.pcpool != null) return false;
        if (whiteboard != null ? !whiteboard.equals(that.whiteboard) : that.whiteboard != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = beamer != null ? beamer.hashCode() : 0;
        result = 31 * result + (pcpool != null ? pcpool.hashCode() : 0);
        result = 31 * result + (whiteboard != null ? whiteboard.hashCode() : 0);
        result = 31 * result + (overhead != null ? overhead.hashCode() : 0);
        return result;
    }

    public Boolean getBeamer() {
        return beamer;
    }

    public void setBeamer(Boolean beamer) {
        this.beamer = beamer;
    }

    public Boolean getPcpool() {
        return pcpool;
    }

    public void setPcpool(Boolean pcpool) {
        this.pcpool = pcpool;
    }

    public Boolean getWhiteboard() {
        return whiteboard;
    }

    public void setWhiteboard(Boolean whiteboard) {
        this.whiteboard = whiteboard;
    }

    public Boolean getOverhead() {
        return overhead;
    }

    public void setOverhead(Boolean overhead) {
        this.overhead = overhead;
    }
}
