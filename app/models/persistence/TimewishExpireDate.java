package models.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;

/**
 * @author fabian
 *         on 19.06.14.
 */
@Entity
@Table(name = "TBLTIMEWISH_EXPIREDATE")
public class TimewishExpireDate extends AbstractEntity {

    /**
     * Expire date needed in the ui for timewishes
     */
    @Column(name = "EXPIREDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar expiredate;

    public Calendar getExpiredate() {
        return expiredate;
    }

    public void setExpiredate(Calendar expiredate) {
        this.expiredate = expiredate;
    }
}
