package models.persistence.scheduletree;

import models.persistence.AbstractEntity;
import org.hibernate.annotations.DiscriminatorOptions;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 27.01.14.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLNODE")
@DiscriminatorOptions(force = true)
/**superclass of all nodes. required to construct the schedule tree*/
public abstract class Node extends AbstractEntity {

    /**
     * each node knows his parent node
     */
    @ManyToOne(optional = true)
    public Node parent;

    /**
     * each node has a list of children
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    public List<Node> children;

}
