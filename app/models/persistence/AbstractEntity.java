package models.persistence;

import javax.persistence.*;

/**
 * Created by fabian on 03.02.14.
 */
@MappedSuperclass
public abstract class AbstractEntity {

    /**
     * database id
     */
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
