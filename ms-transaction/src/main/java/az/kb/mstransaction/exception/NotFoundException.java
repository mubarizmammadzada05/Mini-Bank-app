package az.kb.mstransaction.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message, Long id) {
        super(message + " " + id);
    }
}
