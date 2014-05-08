package logic.generator.schedulerater.rater;

/**
 * @author fabian
 *         on 05.05.14.
 */
public enum ERaters {


    /**
     * enum constant
     */
    WISHTIME_RATER(new WishTimeRater(), 2f),
    /**
     * enum constant
     */
    UNPOPULAR_RATER(new UnpopularSlotRater(), 1f);

    /**
     * the rater
     */
    private Rater rater;

    /**
     * priority of this rater
     */
    private Float priority;

    public Float getPriority() {
        return priority;
    }

    public Rater getRater() {
        return rater;
    }

    private ERaters(Rater rater, Float priority) {
        this.rater = rater;
        this.priority = priority;
    }

}
