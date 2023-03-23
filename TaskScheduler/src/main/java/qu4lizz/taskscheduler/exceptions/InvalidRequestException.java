package qu4lizz.taskscheduler.exceptions;

public class InvalidRequestException extends Exception {
    public InvalidRequestException() {
        super("Invalid request");
    }
    public InvalidRequestException(String message) {
        super(message);
    }

}
