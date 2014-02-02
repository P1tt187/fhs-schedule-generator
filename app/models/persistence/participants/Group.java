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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Group group = (Group) o;

        if (course != null ? !course.equals(group.course) : group.course != null) return false;
        if (parent != null ? !parent.equals(group.parent) : group.parent != null) return false;
        if (subGroups != null ? !subGroups.equals(group.subGroups) : group.subGroups != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (subGroups != null ? subGroups.hashCode() : 0);
        result = 31 * result + (course != null ? course.hashCode() : 0);
        return result;
    }
}
