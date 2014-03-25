package models.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * @author fabian
 *         on 03.02.14.
 */
@MappedSuperclass
//@Access(AccessType.PROPERTY)
public abstract class AbstractEntity {


    /**
     * the version
     */
    @Version
    @Transient
    @JsonIgnore
    private Long version = 1L;

    /**
     * database id
     */
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }


}
