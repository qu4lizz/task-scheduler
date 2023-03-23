package qu4lizz.taskscheduler.task;

import qu4lizz.taskscheduler.utils.Utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Task {
    private State state = State.READY_FOR_SCHEDULE;
    private final UserTask userTask;
    private final Thread thread;
    private Consumer<Task> onFinishedOrKilled;
    private Consumer<Task> onPaused;
    private Consumer<Task> onContinue;
    private Consumer<Task> onContextSwitch;
    private Consumer<Task> onStarted;
    private BiConsumer<Task, String> onResourceAcquire;
    private Consumer<String> onResourceRelease;
    private Consumer<Double> onProgressUpdate;
    private final Object stateLock = new Object();
    private final Object pauseLock = new Object();
    private final Object waitForFinishLock = new Object();
    private final Object contextSwitchLock = new Object();
    private final Object resourceLock = new Object();
    private double progress;
    private final Object progressLock = new Object();

    public enum State {
        NOT_SCHEDULED,
        READY_FOR_SCHEDULE,
        RUNNING,
        PAUSE_REQUESTED,
        PAUSED,
        CONTINUE_REQUESTED,
        CONTEXT_SWITCH_REQUESTED,
        CONTEXT_SWITCHED,
        WAITING_FOR_RESOURCE,
        CONTINUE_AFTER_RESOURCE,
        FINISHED,
        KILL_REQUESTED,
        KILLED
    }

    public Task(UserTask userTask) {
        this.userTask = userTask;
        thread = new Thread(() -> {
            try {
                userTask.execute();
            }
            finally {
                finish();
            }
        });
    }

    public State getState() {
        synchronized (stateLock) {
            return state;
        }
    }
    public void setState(State state) {
        synchronized (stateLock) {
            this.state = state;
        }
    }
    public int getPriority() {
        return userTask.getPriority();
    }
    public void setPriority(int priority) { userTask.setPriority(priority); }
    public Object getStateLock() {
        return stateLock;
    }
    public Object getContextSwitchLock() { return contextSwitchLock; }
    public UserTask getUserTask() {
        return userTask;
    }
    public Consumer<Task> getOnPaused() { return onPaused; }
    public Consumer<Task> getOnFinishedOrKilled() { return onFinishedOrKilled; }
    public Consumer<Task> getOnContextSwitch() { return onContextSwitch; }

    public BiConsumer<Task, String> getOnResourceAcquire() { return onResourceAcquire; }

    public Consumer<String> getOnResourceRelease() { return onResourceRelease; }
    public void setOnProgressUpdate(Consumer<Double> onProgressUpdate) { this.onProgressUpdate = onProgressUpdate; }

    public void setConsumers(Consumer<Task> onContinue, Consumer<Task> onFinished,
                             Consumer<Task> onPaused, Consumer<Task> onContextSwitch,
                             Consumer<Task> onStarted, Consumer<String> onResourceReleased,
                             BiConsumer<Task, String> onResourceAcquired) {
        this.onContinue = onContinue;
        this.onFinishedOrKilled = onFinished;
        this.onPaused = onPaused;
        this.onContextSwitch = onContextSwitch;
        this.onStarted = onStarted;
        this.onResourceAcquire = onResourceAcquired;
        this.onResourceRelease = onResourceReleased;
    }
    public Object getPauseLock() { return pauseLock; }
    public Object getWaitForFinishLock() { return waitForFinishLock; }
    public double getProgress() { synchronized (progressLock) { return progress; } }
    public void addProgress(double progress) {
        synchronized (progressLock) {
            this.progress += progress;
        }
        if (onProgressUpdate != null)
            onProgressUpdate.accept(getProgress());
    }

    // method called by TaskScheduler, not by user
    public void start() {
        synchronized (stateLock) {
            switch (state) {
                case READY_FOR_SCHEDULE -> {
                    state = State.RUNNING;
                    thread.start();
                }
                case CONTINUE_REQUESTED -> {
                    state = State.RUNNING;
                    synchronized (pauseLock) {
                        pauseLock.notify();
                    }
                }
                case CONTEXT_SWITCHED -> {
                    state = State.RUNNING;
                    synchronized (contextSwitchLock) {
                        contextSwitchLock.notify();
                    }
                }
                case CONTINUE_AFTER_RESOURCE -> {
                    state = State.RUNNING;
                    synchronized (resourceLock) {
                        resourceLock.notify();
                    }
                }
                // TODO: what happens if task is in context switch or pause or resource acquire?
                default -> throw new RuntimeException("Task is not ready for start");
            }
        }
    }

    public void requestPause() {
        synchronized (stateLock) {
            switch (state) {
                // TODO: what happens if task is in context switch?
                case RUNNING, CONTEXT_SWITCH_REQUESTED, CONTEXT_SWITCHED -> state = State.PAUSE_REQUESTED;
                case CONTINUE_REQUESTED -> state = State.PAUSED;
                default -> throw new RuntimeException("Task can't be paused");
            }
        }
    }

    public void requestContextSwitch() {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                state = State.CONTEXT_SWITCH_REQUESTED;
            } else {
                throw new RuntimeException("Task is not ready for context switch");
            }
        }
    }

    public void requestKill() {
        synchronized (stateLock) {
            switch (state) {
                case KILLED, FINISHED -> throw new RuntimeException("Task can't be killed");
                default -> state = State.KILL_REQUESTED;
            }
        }
    }

    public void requestContinueOrStart() {
        synchronized (stateLock) {
            switch (state) {
                case NOT_SCHEDULED -> {
                    state = State.READY_FOR_SCHEDULE;
                    onStarted.accept(this);
                }
                case PAUSED -> {
                    state = State.CONTINUE_REQUESTED;
                    onContinue.accept(this);
                }
                case PAUSE_REQUESTED -> state = State.RUNNING;
                case RUNNING, CONTINUE_REQUESTED, READY_FOR_SCHEDULE, CONTEXT_SWITCH_REQUESTED, CONTEXT_SWITCHED -> { }
                default -> throw new RuntimeException("Task can't be started or continued");
            }
        }
    }

    public void finish() {
        synchronized (stateLock) {
            switch (state) {
                case RUNNING -> {
                    state = State.FINISHED;
                    onFinishedOrKilled.accept(this);
                    synchronized (waitForFinishLock) {
                        waitForFinishLock.notifyAll();
                    }
                }
                case FINISHED, KILLED -> { }
                default -> throw new RuntimeException("Task can't finish");
            }
        }
    }

    public final void waitForFinish() {
        synchronized (waitForFinishLock) {
            synchronized (stateLock) {
                if (state == Task.State.FINISHED) {
                    return;
                }
            }
            try {
                waitForFinishLock.wait();
            } catch (InterruptedException ignore) { }
        }
    }

    public void blockForResource() {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                state = State.WAITING_FOR_RESOURCE;
                onPaused.accept(this);
            } else {
                throw new RuntimeException("Task is not ready for blocking");
            }
        }
        synchronized (resourceLock) {
            try {
                resourceLock.wait();
            } catch (InterruptedException ignore) { }
        }
    }
    public void unblockForResource() {
        synchronized (stateLock) {
            if (state == State.WAITING_FOR_RESOURCE) {
                state = State.CONTINUE_AFTER_RESOURCE;
                onContinue.accept(this);
            } else {
                throw new RuntimeException("Task is not ready for unblocking");
            }
        }
    }

    /**
     * @return True if running tasks are less than maxTasks and task doesn't have a start date or
     * start date is in the past and task doesn't have an end date or end date is in the future
     */
    public boolean canBeStarted() {
        return (userTask.getStartDate() == null || (userTask.getStartDate().compareTo(Utils.getCurrentDateAndTime()) <= 0 &&
                        (userTask.getEndDate() == null || userTask.getEndDate().compareTo(Utils.getCurrentDateAndTime()) > 0)));
    }
    public boolean isOutOfDate() {
        return userTask.getEndDate() != null && userTask.getEndDate().compareTo(Utils.getCurrentDateAndTime()) <= 0;
    }
    public boolean cannotBeStartedYet() {
        return userTask.getStartDate() != null && userTask.getStartDate().compareTo(Utils.getCurrentDateAndTime()) > 0;
    }
}
