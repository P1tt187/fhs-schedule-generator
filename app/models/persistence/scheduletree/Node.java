package models.persistence.scheduletree;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import models.persistence.AbstractEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

/**
 * @author fabian
 *         on 27.01.14.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLNODE")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
//@DiscriminatorOptions(force = true)
/**superclass of all nodes. required to construct the schedule tree*/
public abstract class Node extends AbstractEntity {

    /**
     * each node knows his parent node
     */
    @JsonBackReference("nodeChildren")
    @ManyToOne(optional = true)
    private Node parent;

    /**
     * each node has a list of children
     */
    @JsonManagedReference("nodeChildren")
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.EAGER)
    private List<Node> children;

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
        sb.append("children=").append(children);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (children != null ? !(children.containsAll(node.children) && children.size() == node.children.size()) : node.children != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int ret = 0;
        if (children != null) {
            for (Node n : children) {
                ret = 37 * ret + n.hashCode();
            }
        }
        return ret;
    }
}
