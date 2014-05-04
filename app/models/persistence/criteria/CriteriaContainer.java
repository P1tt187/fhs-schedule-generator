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
    public Long calculateDifficultLevel() {
        return criterias.stream().parallel().mapToLong(c -> {
            Long ret = c.getPriority().getSortIndex() + (c.isTolerance() ? 0l : 2l);
            if (!(c instanceof RoomCriteria)) {
                return ret;
            }
            RoomCriteria rc = (RoomCriteria) c;
            if (rc.getRoom() != null) {
                ret += rc.getRoom().getCriteriaContainer().calculateDifficultLevel();
            }
            if (rc.getHouse() != null) {
                ret += rc.getHouse().getRooms().stream().mapToLong(r -> r.getCriteriaContainer().calculateDifficultLevel()).sum();
            }

            return ret;
        }).sequential().sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CriteriaContainer that = (CriteriaContainer) o;

        if (criterias != null ? !(criterias.containsAll(that.criterias) && criterias.size() == that.criterias.size()) : that.criterias != null) return false;

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
