package qu4lizz.taskscheduler.exceptions;

public class DeadlockException extends Exception {
    public DeadlockException() {
        super("Deadlock detected");
    }

    public DeadlockException(String message) {
        super(message);
    }
}
