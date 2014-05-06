package logic.generator.schedulerater.rater;

/**
 * @author fabian
 *         on 05.05.14.
 */
public enum ERaters {


    /**
     * enum constant
     */
    WISHTIME_RATER(new WishTimeRater());

    /** the rater */
    private Rater rater;

    public Rater getRater() {
        return rater;
    }

    private ERaters(Rater rater) {
        this.rater = rater;
    }

}
