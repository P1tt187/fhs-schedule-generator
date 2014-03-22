package models.persistence.enumerations;

/**
 * Created by fabian on 07.02.14.
 */
public enum EPriority {
    /** enum constant **/
    HIGH(1),
    /** enum constant **/
    NORMAL(0),
    /** enum constant **/
    LOW(-1);

    /** the sort index */
    private Integer sortIndex;

    public Integer getSortIndex() {
        return sortIndex;
    }

    EPriority(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }
}
