package models.persistence;

import org.hibernate.annotations.DiscriminatorOptions;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 27.01.14.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="TBLNODE")
@DiscriminatorOptions(force = true)
public abstract class Node extends Model{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="ID")
    public Integer id;

    @ManyToOne(optional = true)
    public Node parent;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    public List<Node> children;

}
