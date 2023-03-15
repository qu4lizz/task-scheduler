package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.task.Task;
import qu4lizz.taskscheduler.utils.ConcurrentPriorityQueue;
import qu4lizz.taskscheduler.utils.Utils;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TaskScheduler {
    protected final AtomicInteger maxTasks;
    protected Queue<Task> waitingTasksToBeStarted;
    protected HashSet<Task> waitingTasksWithoutStartSignal;
    protected HashSet<Task> activeTasks;
    protected final Object lock = new Object();
    protected ConcurrentPriorityQueue<Task> tasksWithEndDate;
    protected final Object killerLock = new Object();
    protected final AtomicBoolean enabled = new AtomicBoolean(true);

    public TaskScheduler(int numOfConcurrentTasks) {
        maxTasks = new AtomicInteger(numOfConcurrentTasks);
        activeTasks = new HashSet<>();
        tasksWithEndDate = new ConcurrentPriorityQueue<>((x, y) ->
                Utils.dateDifference(y.getUserTask().getEndDate(), x.getUserTask().getEndDate()));

        Thread killerThread = new Thread(() -> {
            int timeToSleep = -1;
            while (enabled.get()) { // TODO: set to false when X is pressed
                synchronized (killerLock) {
                    try {
                        if (timeToSleep == -1) {
                            killerLock.wait();
                        } else {
                            if (timeToSleep > 0)
                                killerLock.wait(timeToSleep);
                        }
                    } catch (InterruptedException ignore) { }
                }
                synchronized (lock) {
                    for (var task : tasksWithEndDate) {
                        if (task.isOutOfDate()) {
                            try {
                                handleTaskFinishedOrKilled(task);
                            } catch (InvalidRequestException ignore) { }
                        }
                    }
                }
                timeToSleep = Utils.dateDifference(tasksWithEndDate.peek().getUserTask().getEndDate(), Utils.getCurrentDateAndTime());
            }
        });
        killerThread.start();
    }

    public void addTask(Task task, boolean shouldStart) throws InvalidRequestException {
        addTaskActions(task);
        synchronized (lock) {
            if (!shouldStart) {
                waitingTasksWithoutStartSignal.add(task);
                task.setState(Task.State.NOT_SCHEDULED);
            }
            else if (taskCanBeStarted(task) && shouldStart) {
                startTask(task);

            } else if (task.cannotBeStartedYet()) {
                waitingTasksToBeStarted.add(task);
            } else if (task.isOutOfDate()) {
                handleTaskOutOfDate(task); // TODO: Do I need this?
            } else {
                throw new RuntimeException("This should never happen");
            }
        }
    }

    protected void startNextTask() throws InvalidRequestException {
        for (var nextTask : waitingTasksToBeStarted) {
            if (taskCanBeStarted(nextTask)) {
                startTask(nextTask);
                if (nextTask.getUserTask().getEndDate() != null && !tasksWithEndDate.contains(nextTask)) {
                    tasksWithEndDate.add(nextTask);
                    synchronized (killerLock) {
                        killerLock.notify();
                    }
                }
                waitingTasksToBeStarted.remove(nextTask);
            }
            else if (nextTask.cannotBeStartedYet()) { }
            else if (nextTask.isOutOfDate()) {
                handleTaskOutOfDate(nextTask);
            }
            else {
                throw new RuntimeException("This should never happen");
            }
        }
    }
    protected void addTaskActions(Task task) {
        task.setActions(this::handleTaskContinue, this::handleTaskFinishedOrKilled, this::handleTaskPaused,
                this::handleTaskContextSwitch, this::handleTaskStated);
    }
    protected void startTask(Task task) throws InvalidRequestException {
        activeTasks.add(task);
        task.start();
        if (task.getUserTask().getEndDate() != null && !tasksWithEndDate.contains(task)) {
            tasksWithEndDate.add(task);
            synchronized (killerLock) {
                killerLock.notify();
            }
        }
    }
    protected void handleTaskOutOfDate(Task task) {
        waitingTasksToBeStarted.remove(task);
        // TODO: Send notification to user
    }
    protected void handleTaskStated(Task task) throws InvalidRequestException {
        addTask(task, true);
        waitingTasksWithoutStartSignal.remove(task);
    }

    protected void handleTaskPaused(Task task) throws InvalidRequestException {
        synchronized (lock) {
            activeTasks.remove(task);
            startNextTask();
        }
    }

    protected void handleTaskContinue(Task task) throws InvalidRequestException {
        synchronized (lock) {
            if (taskCanBeStarted(task)) {
                activeTasks.add(task);
                task.start();
            } else if (task.isOutOfDate()) {
                handleTaskOutOfDate(task);
            } else if (maxTasks.equals(activeTasks.size()) || task.cannotBeStartedYet()) {
                waitingTasksToBeStarted.add(task);
            }
            else {
                throw new RuntimeException("This should never happen");
            }
        }
    }
    protected void handleTaskFinishedOrKilled(Task task) throws InvalidRequestException {
        synchronized (lock) {
            activeTasks.remove(task);
            startNextTask();
        }
    }

    protected void handleTaskContextSwitch(Task task) throws InvalidRequestException {
        synchronized (lock) {
            activeTasks.remove(task);
            startNextTask();
            waitingTasksToBeStarted.add(task);
        }
    }

    protected boolean taskCanBeStarted(Task task) {
        return activeTasks.size() < maxTasks.get() && task.canBeStarted();
    }
}
