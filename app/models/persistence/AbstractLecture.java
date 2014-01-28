package models.persistence;

import javax.persistence.*;

/**
 * Created by fabian on 28.01.14.
 */
@MappedSuperclass
public abstract class AbstractLecture {


    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer id;
}
