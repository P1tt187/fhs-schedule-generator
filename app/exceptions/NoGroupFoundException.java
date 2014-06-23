package exceptions;


import models.persistence.subject.ExerciseSubject;

/**
 * @author fabian
 *         on 21.03.14.
 */
public class NoGroupFoundException extends Exception {

    public NoGroupFoundException() {
        super();
    }

    public NoGroupFoundException(String message) {
        super(message);
    }

    public NoGroupFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoGroupFoundException(Throwable cause) {
        super(cause);
    }

    public NoGroupFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    /** group type that causes the error */
    private String groupType;

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    /** the subject that causes the error*/
    private ExerciseSubject subject;

    public ExerciseSubject getSubject() {
        return subject;
    }

    public void setSubject(ExerciseSubject subject) {
        this.subject = subject;
    }
}
