package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.task.UserTask;
import qu4lizz.taskscheduler.utils.ConcurrentPriorityQueue;
import qu4lizz.taskscheduler.utils.Utils;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TaskScheduler {
    private final AtomicInteger maxTasks;
    protected Queue<UserTask> nonActiveTasks;
    protected HashSet<UserTask> activeTasks;
    private final Object lock = new Object();
    private ConcurrentPriorityQueue<UserTask> tasksWithEndDate;
    private final Object killerLock = new Object();

    public TaskScheduler(int noOfTasks) {
        maxTasks = new AtomicInteger(noOfTasks);
        activeTasks = new HashSet<>();
        tasksWithEndDate = new ConcurrentPriorityQueue<>((x, y) ->
                Utils.dateDifference(y.getEndDate(), x.getEndDate()));

        Thread killerThread = new Thread(() -> {
            int timeToSleep = -1;
            while (true) {
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
                    for (UserTask task : tasksWithEndDate) {
                        if (taskIsOutOfDate(task)) {
                            try {
                                handleTaskFinishedOrKilled(task);
                            } catch (InvalidRequestException ignore) { }
                        }
                    }
                }
                timeToSleep = Utils.dateDifference(tasksWithEndDate.peek().getEndDate(), Utils.getCurrentDateAndTime());
            }
        });
        killerThread.start();
    }

    public void addTask(UserTask task) throws InvalidRequestException {
        task.setActions(this::handleTaskContinue, this::handleTaskFinishedOrKilled, this::handleTaskPaused);
        synchronized (lock) {
            if (taskCanBeStarted(task)) {
                activeTasks.add(task);
                task.start();
                if (task.getEndDate() != null) {
                    tasksWithEndDate.add(task);
                    synchronized (killerLock) {
                        killerLock.notify();
                    }
                }
            } else if (taskCannotBeStartedYet(task)) {
                nonActiveTasks.add(task);
            } else if (taskIsOutOfDate(task)) {
                handleTaskOutOfDate(task);
            } else {
                throw new RuntimeException("This should never happen");
            }
        }
    }

    private void handleTaskOutOfDate(UserTask task) {
        nonActiveTasks.remove(task);
        // TODO: Send notification to user
    }
    private void startNextTask(UserTask task) throws InvalidRequestException {
        for (var nextTask : nonActiveTasks) {
            if (taskCanBeStarted(nextTask)) {
                activeTasks.add(nextTask);
                nextTask.start();
            }
            else if (taskCannotBeStartedYet(nextTask)) {
                // ignore
            } else if (taskIsOutOfDate(nextTask)) {
                handleTaskOutOfDate(nextTask);
            }
            else {
                throw new RuntimeException("This should never happen");
            }
        }
    }
    private void handleTaskPaused(UserTask task) throws InvalidRequestException {
        synchronized (lock) {
            activeTasks.remove(task);
            startNextTask(task);
        }
    }

    private void handleTaskContinue(UserTask task) throws InvalidRequestException {
        synchronized (lock) {
            if (taskCanBeStarted(task)) {
                activeTasks.add(task);
                task.start();
            } else if (taskIsOutOfDate(task)) {
                handleTaskOutOfDate(task);
            } else if (maxTasks.equals(activeTasks.size()) || taskCannotBeStartedYet(task)) {
                nonActiveTasks.add(task);
            }
            else {
                throw new RuntimeException("This should never happen");
            }
        }
    }
    private void handleTaskFinishedOrKilled(UserTask task) throws InvalidRequestException {
        synchronized (lock) {
            activeTasks.remove(task);
            startNextTask(task);
        }
    }

    /**
     * @param task Task to be checked
     * @return True if running tasks are less than maxTasks and task doesn't have a start date or
     * start date is in the past and task doesn't have an end date or end date is in the future
     */
    private boolean taskCanBeStarted(UserTask task) {
        return activeTasks.size() < maxTasks.get() && (task.getStartDate() == null ||
                (task.getStartDate().compareTo(Utils.getCurrentDateAndTime()) <= 0 &&
                        (task.getEndDate() == null || task.getEndDate().compareTo(Utils.getCurrentDateAndTime()) > 0)));
    }
    private boolean taskIsOutOfDate(UserTask task) {
        return task.getEndDate() != null && task.getEndDate().compareTo(Utils.getCurrentDateAndTime()) <= 0;
    }
    private boolean taskCannotBeStartedYet(UserTask task) {
        return task.getStartDate() != null && task.getStartDate().compareTo(Utils.getCurrentDateAndTime()) > 0;
    }
}
