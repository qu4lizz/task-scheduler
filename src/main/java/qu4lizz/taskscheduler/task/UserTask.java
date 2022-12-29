package qu4lizz.taskscheduler.task;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.utils.Utils;

/**
 * A class that represents a task that a user can create.
 * User that wants his Task needs to create Task class and derive it from this class,
 * then create GUI class and derive it from NewTask class, then register it in tasks.txt.
 */
public abstract class UserTask implements ITask {
    private final Task task;
    private String name;
    private int time = -1;
    private String startDate;
    private String endDate;
    private int priority;
    private int numOfThreads;

    /**
     *
     * @param name Name of the task
     * @param numOfThreads Number of threads to use
     * <p> Priority is 5 by default </p>
     */
    public UserTask(String name, int numOfThreads) {
        this(name, 5, numOfThreads);
    }
    /**
     *
     * @param name Name of the task
     * @param priority Priority of the task
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, int numOfThreads) {
        this.name = name;
        this.priority = priority;
        this.numOfThreads = numOfThreads;
        task = new Task(this);
    }
    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, String startDate, int numOfThreads) {
        this(name, 5, startDate, numOfThreads);
    }
    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param priority Priority of the task
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, String startDate, int numOfThreads) {
        this.name = name;
        this.startDate = startDate;
        this.priority = priority;
        this.numOfThreads = numOfThreads;
        task = new Task(this);
    }

    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param endDate Date at which the task should end
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, String startDate, String endDate, int numOfThreads) {
        this(name, 5, startDate, endDate, numOfThreads);
    }

    /**
     * @param name Name of the task
     * @param priority Priority of the task
     * @param startDate Date at which the task should start
     * @param endDate Date at which the task should end
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, String startDate, String endDate, int numOfThreads) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.numOfThreads = numOfThreads;
        this.task = new Task(this);
    }

    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param time Time in seconds for which the task should run
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, String startDate, int time, int numOfThreads) {
        this(name, 5, startDate, time, numOfThreads);
    }

    /**
     * @param name Name of the task
     * @param priority Priority of the task
     * @param startDate Date at which the task should start
     * @param time Time in seconds for which the task should run
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, String startDate, int time, int numOfThreads) {
        this(name, priority, startDate, Utils.calculateEndDate(startDate, time), numOfThreads);
    }

    public final String getName() { return name; }
    public final int getPriority() { return priority; }
    public final String getStartDate() { return startDate; }
    public final String getEndDate() { return endDate; }

    public final void start() throws InvalidRequestException { task.start(); }
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
