package models.persistence.criteria;

import models.persistence.AbstractEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by fabian on 04.02.14.
 */
@Entity
@Table(name = "TBLCRITERIACONTAINER")
public class CriteriaContainer extends AbstractEntity {

    /** criterias */
    //@JoinColumn(name = "FK_CRITERIA", referencedColumnName = "ID")
    @OneToMany(cascade = CascadeType.ALL,targetEntity = AbstractCriteria.class)
    private List<AbstractCriteria> criterias;


    public List<AbstractCriteria> getCriterias() {
        return criterias;
    }

    public void setCriterias(List<AbstractCriteria> criterias) {
        this.criterias = criterias;
    }
}
