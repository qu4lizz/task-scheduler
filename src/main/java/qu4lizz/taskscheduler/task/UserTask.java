package qu4lizz.taskscheduler.task;

import qu4lizz.taskscheduler.exceptions.InvalidArgumentException;
import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.utils.Utils;

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
    public UserTask(String name, int numOfThreads) throws InvalidArgumentException {
        this(name, 5, numOfThreads);
    }
    /**
     *
     * @param name Name of the task
     * @param priority Priority of the task
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, int numOfThreads) throws InvalidArgumentException {
        checkArguments(priority, numOfThreads);
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
    public UserTask(String name, String startDate, int numOfThreads) throws InvalidArgumentException {
        this(name, 5, startDate, numOfThreads);
    }
    /**
     * @param name Name of the task
     * @param startDate Date at which the task should start
     * @param priority Priority of the task
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, String startDate, int numOfThreads) throws InvalidArgumentException {
        checkArguments(priority, numOfThreads);
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
    public UserTask(String name, String startDate, String endDate, int numOfThreads) throws InvalidArgumentException {
        this(name, 5, startDate, endDate, numOfThreads);
    }

    /**
     * @param name Name of the task
     * @param priority Priority of the task
     * @param startDate Date at which the task should start
     * @param endDate Date at which the task should end
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, String startDate, String endDate, int numOfThreads) throws InvalidArgumentException {
        checkArguments(priority, numOfThreads);
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
    public UserTask(String name, String startDate, int time, int numOfThreads) throws InvalidArgumentException {
        this(name, 5, startDate, time, numOfThreads);
    }

    /**
     * @param name Name of the task
     * @param priority Priority of the task
     * @param startDate Date at which the task should start
     * @param time Time in seconds for which the task should run
     * @param numOfThreads Number of threads to use
     */
    public UserTask(String name, int priority, String startDate, int time, int numOfThreads) throws InvalidArgumentException {
        this(name, priority, startDate, Utils.calculateEndDate(startDate, time), numOfThreads);
    }

    public final String getName() { return name; }
    public final int getPriority() { return priority; }
    public final String getStartDate() { return startDate; }
    public final String getEndDate() { return endDate; }
    public final int getNumOfThreads() { return numOfThreads; }

    private void checkArguments(int priority, int threads) throws InvalidArgumentException {
        if (priority < 1 || priority > 10) {
            throw new InvalidArgumentException("Priority must be between 1 and 10");
        }
        if (threads < 1) {
            throw new InvalidArgumentException("Threads must be greater than 0");
        }
    }

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
