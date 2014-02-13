package models.persistence;

import javax.persistence.*;

/**
 * Created by fabian on 03.02.14.
 */
@MappedSuperclass
//@Access(AccessType.PROPERTY)
public abstract class AbstractEntity {


    @Version
    protected long serialVersionUID=1L;

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

    public long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setSerialVersionUID(long serialVersionUID) {
        this.serialVersionUID = serialVersionUID;
    }
}
