package qu4lizz.taskscheduler.task;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.utils.Utils;

public abstract class UserTask implements ITask {
    private final Task task;
    private String name;
    private int time = -1;
    private String startDate = null;
    private String endDate = null;
    private int priority;

    /**
     *
     * @param name Name of the task
     * <p> Priority is 5 by default </p>
     */
    public UserTask(String name) {
        this(name, 5);
    }
    /**
     *
     * @param name Name of the task
     * @param priority Priority of the task
     */
    public UserTask(String name, int priority) {
        this.name = name;
        this.priority = priority;
        task = new Task(this);
    }
    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     */
    public UserTask(String name, String startDate) {
        this(name, 5, startDate);
    }
    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param priority Priority of the task
     */
    public UserTask(String name, int priority, String startDate) {
        this.name = name;
        this.startDate = startDate;
        this.priority = priority;
        task = new Task(this);
    }

    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param endDate Date at which the task should end
     */
    public UserTask(String name, String startDate, String endDate) {
        this(name, 5, startDate, endDate);
    }

    /**
     * @param name Name of the task
     * @param priority Priority of the task
     * @param startDate Date at which the task should start
     * @param endDate Date at which the task should end
     */
    public UserTask(String name, int priority, String startDate, String endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.task = new Task(this);
    }

    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param time Time in seconds for which the task should run
     */
    public UserTask(String name, String startDate, int time) {
        this(name, 5, startDate, time);
    }

    /**
     * @param name Name of the task
     * @param priority Priority of the task
     * @param startDate Date at which the task should start
     * @param time Time in seconds for which the task should run
     */
    public UserTask(String name, int priority, String startDate, int time) {
        this(name, priority, startDate, Utils.calculateEndDate(startDate, time));
    }

    public final String getName() { return name; }
    public final int getPriority() { return priority; }
    public final String getStartDate() { return startDate; }
    public final String getEndDate() { return endDate; }

    public final void start() throws InvalidRequestException {
        task.start();
    }
    public final void setActions(Task.Action onContinue, Task.Action onFinished, Task.Action onPaused) {
        task.setActions(onContinue, onFinished, onPaused);
    }

    protected final void checkForPause() throws InvalidRequestException {
        boolean shouldPause = false;
        synchronized (task.getStateLock()) {
            if (task.getState() == Task.State.PAUSE_REQUESTED) {
                task.setState(Task.State.PAUSED);
                task.getOnPaused().act(this);
                shouldPause = true;
            }
        }
        if (shouldPause) {
            synchronized (task.getPauseLock()) {
                try {
                    task.getPauseLock().wait();
                } catch (InterruptedException ignore) { }
            }
        }
    }
    public final void waitForFinish() {
        synchronized (task.getWaitForFinishLock()) {
            synchronized (task.getStateLock()) {
                if (task.getState() == Task.State.FINISHED) {
                    return;
                }
            }
            try {
                task.getWaitForFinishLock().wait();
            } catch (InterruptedException ignore) { }
        }
    }
}
