package models.persistence.lecture;

import javax.persistence.*;
import java.util.List;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name="TBLPARALLELLECTURE")
public class ParallelLecture extends AbstractLecture{

    @JoinColumn(name = "fk_lecture")
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    public List<Lecture> lectures;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ParallelLecture that = (ParallelLecture) o;

        return !(lectures != null ? !lectures.equals(that.lectures) : that.lectures != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lectures != null ? lectures.hashCode() : 0);
        return result;
    }
}
