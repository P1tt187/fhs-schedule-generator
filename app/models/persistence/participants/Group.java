package models.persistence.participants;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@DiscriminatorValue("GROUP")
public class Group extends Participant {

    @ManyToOne
    public Group parent;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    public List<Group> subGroups;

    @ManyToOne
    public Course course;

}
