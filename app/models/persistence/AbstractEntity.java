package models.persistence;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;

/**
 * Created by fabian on 03.02.14.
 */
@MappedSuperclass
//@Access(AccessType.PROPERTY)
public abstract class AbstractEntity {


    /**
     * the version
     */
    @Version
    @Generated(GenerationTime.ALWAYS)
    private Long serialVersionUID ;

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


    public Long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setSerialVersionUID(Long serialVersionUID) {
        this.serialVersionUID = serialVersionUID;
    }


}
