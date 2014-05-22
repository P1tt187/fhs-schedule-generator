package models.persistence.enumerations;

/**
 * @author fabian on 07.02.14.
 */
public enum EDuration {
    /**
     * enum constant
     */
    WEEKLY(0, "w"),

    /**
     * enum constant
     */
    UNWEEKLY(12, "uw"),

    /**
     * enum constant
     */
    EVEN(0, "g"),

    /**
     * enum constant
     */
    UNEVEN(0, "u");

    /**
     * sortindex
     */
    private int sortIndex;
    /**
     * short name for schedule export
     */
    private String shortName;

    public int getSortIndex() {
        return sortIndex;
    }

    public String getShortName() {
        return shortName;
    }

    EDuration(int sortIndex, String shortName) {
        this.sortIndex = sortIndex;
        this.shortName = shortName;
    }

}
