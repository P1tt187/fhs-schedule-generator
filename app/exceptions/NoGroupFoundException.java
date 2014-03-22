package exceptions;

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
}
