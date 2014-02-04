package models.persistence;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by fabian on 03.02.14.
 */
@MappedSuperclass
public abstract class AbstractEntity extends Model {

    /**
     * database id
     */
    @Id
    @Column(name = "THE_ID", nullable = false)
    @Constraints.Required
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
}
