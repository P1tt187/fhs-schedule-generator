package models.persistence;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Created by fabian on 03.02.14.
 */
@MappedSuperclass
public abstract class AbstractEntity extends Model {

    /**
     * database id
     */
    @Id
    @Column(name = "ID", nullable = false)
    @Constraints.Required
    public Long id;
}
