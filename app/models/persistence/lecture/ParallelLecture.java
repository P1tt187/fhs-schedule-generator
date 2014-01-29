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
}
