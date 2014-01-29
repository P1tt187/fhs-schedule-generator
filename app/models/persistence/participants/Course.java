package models.persistence.participants;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@DiscriminatorValue("COURSE")
public class Course extends Participant {

    @Column(name="NAME")
    public String name;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "course")
    public List<Group> groups;
}
