package models.persistence.criteria;

import models.persistence.AbstractEntity;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 04.02.14.
 */
@Entity
@Table(name = "TBLCRITERIACONTAINER")
public class CriteriaContainer extends AbstractEntity {

    /** criterias */
    @JoinColumn(name = "fk_criteria")
    @OneToMany(cascade = CascadeType.ALL)
    public List<AbstractCriteria> criterias;

}
