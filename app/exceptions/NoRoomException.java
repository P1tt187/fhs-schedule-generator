package exceptions;

import models.persistence.lecture.Lecture;

/**
 * @author fabian
 *         on 17.06.14.
 */
public class NoRoomException extends Exception {

    public NoRoomException() {
    }

    public NoRoomException(String message) {
        super(message);
    }

    public NoRoomException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRoomException(Throwable cause) {
        super(cause);
    }

    public NoRoomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    /** the lecture that causes this error*/
    private Lecture lecture;

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }
}
