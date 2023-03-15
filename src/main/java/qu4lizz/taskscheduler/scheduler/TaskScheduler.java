package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.task.Task;
import qu4lizz.taskscheduler.task.UserTask;
import qu4lizz.taskscheduler.utils.ConcurrentPriorityQueue;
import qu4lizz.taskscheduler.utils.Utils;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TaskScheduler {
    private final AtomicInteger maxTasks;
    protected Queue<Task> nonActiveTasks;
    protected HashSet<Task> activeTasks;
    protected final Object lock = new Object();
    protected ConcurrentPriorityQueue<Task> tasksWithEndDate;
    private final Object killerLock = new Object();
    protected final AtomicBoolean enabled = new AtomicBoolean(true);

    public TaskScheduler(int numOfConcurrentTasks) {
        maxTasks = new AtomicInteger(numOfConcurrentTasks);
        activeTasks = new HashSet<>();
        tasksWithEndDate = new ConcurrentPriorityQueue<>((x, y) ->
                Utils.dateDifference(y.getUserTask().getEndDate(), x.getUserTask().getEndDate()));

        Thread killerThread = new Thread(() -> {
            int timeToSleep = -1;
            while (enabled.get()) {
                synchronized (killerLock) {
                    try {
                        if (timeToSleep == -1) {
                            killerLock.wait();
                        } else {
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

    public void addTask(Task task) throws InvalidRequestException {
        addTaskActions(task);
        synchronized (lock) {
            if (taskCanBeStarted(task)) {
                activeTasks.add(task);
                task.start();
                if (task.getUserTask().getEndDate() != null) {
                    tasksWithEndDate.add(task);
                    synchronized (killerLock) {
                        killerLock.notify();
                    }
                }
            } else if (task.cannotBeStartedYet()) {
                nonActiveTasks.add(task);
            } else if (task.isOutOfDate()) {
                handleTaskOutOfDate(task);
            } else {
                throw new RuntimeException("This should never happen");
            }
        }
    }
    protected void addTaskActions(Task task) {
        task.setActions(this::handleTaskContinue, this::handleTaskFinishedOrKilled, this::handleTaskPaused, null);
    }

    protected void handleTaskOutOfDate(Task task) {
        nonActiveTasks.remove(task);
        // TODO: Send notification to user
    }
    protected void startNextTask() throws InvalidRequestException {
        Task taskToAdd = null;
        for (var nextTask : nonActiveTasks) {
            taskToAdd = nextTask;
            if (taskCanBeStarted(nextTask)) {
                activeTasks.add(nextTask);
                nextTask.start();
                if (nextTask.getUserTask().getEndDate() != null && !tasksWithEndDate.contains(nextTask)) {
                    tasksWithEndDate.add(nextTask);
                    synchronized (killerLock) {
                        killerLock.notify();
                    }
                }
                break;
            }
            else if (nextTask.cannotBeStartedYet()) {
                // ignore
            } else if (nextTask.isOutOfDate()) {
                handleTaskOutOfDate(nextTask);
            }
            else {
                throw new RuntimeException("This should never happen");
            }
        }
        if (taskToAdd != null)
            nonActiveTasks.remove(taskToAdd);
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
                nonActiveTasks.add(task); // TODO: Do I need to do this?
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

    // TODO: fix this
    protected void handleTaskContextSwitch(Task task) throws InvalidRequestException {
        synchronized (lock) {
            if (task.canBeStarted()) {
                activeTasks.add(task);
                task.start();
            } else {
                nonActiveTasks.add(task);
            }
        }
    }

    private boolean taskCanBeStarted(Task task) {
        return activeTasks.size() < maxTasks.get() && task.canBeStarted();
    }
}
