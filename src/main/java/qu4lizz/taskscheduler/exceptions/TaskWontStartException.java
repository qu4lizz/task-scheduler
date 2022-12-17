package qu4lizz.taskscheduler.exceptions;

public class TaskWontStartException extends Exception {
    public TaskWontStartException() {
        super("Task won't start");
    }
    public TaskWontStartException(String message) {
        super(message);
    }

}
