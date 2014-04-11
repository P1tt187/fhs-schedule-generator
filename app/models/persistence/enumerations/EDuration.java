package models.persistence.enumerations;

/**
 * @author fabian on 07.02.14.
 */
public enum EDuration {
    /**
     * enum constant
     */
    WEEKLY(0),

    /**
     * enum constant
     */
    UNWEEKLY(12),

    /**
     * enum constant
     */
    EVEN(0),

    /**
     * enum constant
     */
    UNEVEN(0);

    /** sortindex */
    private int sortIndex;

    public int getSortIndex() {
        return sortIndex;
    }

    EDuration(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}
