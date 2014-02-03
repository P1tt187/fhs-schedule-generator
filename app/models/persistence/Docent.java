package models.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by fabian on 01.02.14.
 */
@Entity
@Table(name="TBLDOCENT")
public class Docent extends AbstractEntity {


    @Column(name = "FIRSTNAME")
    public String firstName;

    @Column(name = "LASTNAME")
    public String lastName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Docent docent = (Docent) o;

        if (firstName != null ? !firstName.equals(docent.firstName) : docent.firstName != null) return false;
        if (id != null ? !id.equals(docent.id) : docent.id != null) return false;
        if (lastName != null ? !lastName.equals(docent.lastName) : docent.lastName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        return result;
    }
}
