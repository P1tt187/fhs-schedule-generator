package models.persistence.lecture;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by fabian on 28.01.14.
 */
@MappedSuperclass
public abstract class AbstractLecture extends Model {


    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
}
