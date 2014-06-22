package exceptions;

import models.persistence.docents.Docent;
import models.persistence.docents.LectureDocent;

import java.util.List;

/**
 * @author fabian
 *         on 18.06.14.
 */
public class DocentsNotAtSameTimeAvailableException extends Exception {
    public DocentsNotAtSameTimeAvailableException() {
    }

    public DocentsNotAtSameTimeAvailableException(String message) {
        super(message);
    }

    public DocentsNotAtSameTimeAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocentsNotAtSameTimeAvailableException(Throwable cause) {
        super(cause);
    }

    public DocentsNotAtSameTimeAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /** the docents that causes the error */
    private List<LectureDocent> docents;

    public List<LectureDocent> getDocents() {
        return docents;
    }

    public void setDocents(List<LectureDocent> docents) {
        this.docents = docents;
    }
}
