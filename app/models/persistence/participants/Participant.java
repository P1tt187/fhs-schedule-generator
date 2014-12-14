package models.persistence.participants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import models.persistence.AbstractEntity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by fabian
 *      on 29.01.14.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "TBLPARTICIPANTS")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class Participant extends AbstractEntity {


    @Column(name = "SIZE", nullable = true)
    private Integer size;

    @JoinColumn(name = "fk_students")
    @OneToMany(fetch = FetchType.EAGER,targetEntity = Student.class)
    private Set<Student> students;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @JsonIgnore
    public abstract Course getCourse();

    @JsonIgnore
    public abstract String getName();

    public abstract LectureParticipant participant2LectureParticipant();

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }
}
