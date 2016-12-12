package Bank;

final public class RejectedException extends Exception {
    private static final long serialVersionUID = 5306521076696198128L;

    public RejectedException(String reason) {
        super(reason);
    }

    public RejectedException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
