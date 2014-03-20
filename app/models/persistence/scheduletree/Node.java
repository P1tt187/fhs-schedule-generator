package models.persistence.scheduletree;

import models.persistence.AbstractEntity;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 27.01.14.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLNODE")
//@DiscriminatorOptions(force = true)
/**superclass of all nodes. required to construct the schedule tree*/
public abstract class Node extends AbstractEntity {

    /**
     * each node knows his parent node
     */
    @ManyToOne(optional = true)
    protected Node parent;

    /**
     * each node has a list of children
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    protected List<Node> children;

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("parent=").append(parent);
        sb.append(", children=").append(children);
        sb.append('}');
        return sb.toString();
    }
}
