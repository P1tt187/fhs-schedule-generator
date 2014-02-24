package models.persistence.criteria;

import models.persistence.AbstractEntity;

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
    @OneToMany(cascade = CascadeType.ALL, targetEntity = AbstractCriteria.class, fetch = FetchType.EAGER)
    private List<AbstractCriteria> criterias;


    public List<AbstractCriteria> getCriterias() {
        return criterias;
    }

    public void setCriterias(List<AbstractCriteria> criterias) {
        this.criterias = criterias;
    }
}
