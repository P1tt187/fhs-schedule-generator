package models.persistence.participants;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 29.01.14.
 */
@Entity
@DiscriminatorValue("COURSE")
public class Course extends Participant {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Course course = (Course) o;

        if (groups != null ? !groups.equals(course.groups) : course.groups != null) return false;
        if (name != null ? !name.equals(course.name) : course.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }


    @Column(name = "NAME", nullable = false)
    public String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    public List<Group> groups;
}
