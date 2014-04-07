package models.persistence.criteria;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import models.persistence.AbstractEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * @author fabian
 *         on 04.02.14.
 */
@Entity
@Table(name = "TBLCRITERIACONTAINER")
public class CriteriaContainer extends AbstractEntity {

    /**
     * criterias
     */
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(cascade = CascadeType.ALL, targetEntity = AbstractCriteria.class, fetch = FetchType.EAGER)
    @OrderBy("priority, tolerance")
    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
    private List<AbstractCriteria> criterias;


    public List<AbstractCriteria> getCriterias() {
        return criterias;
    }

    public void setCriterias(List<AbstractCriteria> criterias) {
        this.criterias = criterias;
    }

    /**
     * costs are used to find out how import it is to place a lecture first
     */
    @JsonIgnore
    public Integer getCost() {
        return criterias.stream().parallel().mapToInt(c -> {
            Integer ret = c.getPriority().getSortIndex() + (c.isTolerance() ? 0 : 2);
            if (c instanceof RoomCriteria) {
                RoomCriteria rc = (RoomCriteria) c;
                if (rc.getRoom() != null) {
                    ret += rc.getRoom().getCriteriaContainer().getCost();
                }
                if (rc.getHouse() != null) {
                    ret += rc.getHouse().getRooms().stream().mapToInt(r -> r.getCriteriaContainer().getCost()).sum();
                }
            }

            return ret;
        }).sequential().sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CriteriaContainer that = (CriteriaContainer) o;

        if (criterias != null ? !criterias.equals(that.criterias) : that.criterias != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return criterias != null ? criterias.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CriteriaContainer{" +
                "criterias=" + criterias +
                '}';
    }
}
