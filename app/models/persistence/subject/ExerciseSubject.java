package models.persistence.subject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by fabian on 07.02.14.
 */
@Entity
@Table(name="TBLEXERCISE_SUBJECT")
public class ExerciseSubject  extends AbstractSubject {


    @Column(name = "GROUPTYPE")
    private String groupType;


    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }


}
