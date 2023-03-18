package qu4lizz.taskscheduler.task;

import qu4lizz.taskscheduler.utils.Utils;

import java.util.Objects;
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
    private final Object stateLock = new Object();
    private final Object pauseLock = new Object();
    private final Object waitForFinishLock = new Object();
    private final Object finishLock = new Object();
    private final Object contextSwitchLock = new Object();
    private final Object resourceLock = new Object();
    private double progress; // = 0

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

    public State getState() { return state; }
    public void setState(State state) {
        this.state = state;
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
    public void setConsumers(Consumer<Task> onContinue, Consumer<Task> onFinished, Consumer<Task> onPaused,
                             Consumer<Task> onContextSwitch, Consumer<Task> onStarted) {
        this.onContinue = onContinue;
        this.onFinishedOrKilled = onFinished;
        this.onPaused = onPaused;
        this.onContextSwitch = onContextSwitch;
        this.onStarted = onStarted;
    }
    public Object getPauseLock() { return pauseLock; }
    public Object getWaitForFinishLock() { return waitForFinishLock; }
    public Object getFinishLock() { return finishLock; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }

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
                    onContinue.accept(this);
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
                default -> throw new RuntimeException("Task is not ready for start");
            }
        }
    }

    public void requestPause() {
        synchronized (stateLock) {
            switch (state) {
                // TODO: what happens if task is in context switch?
                case RUNNING -> state = State.PAUSE_REQUESTED;
                case CONTINUE_REQUESTED -> state = State.PAUSED;
                case PAUSED, PAUSE_REQUESTED -> { }
                default -> throw new RuntimeException("Task is not ready for pause");
            }
        }
    }

    public void requestContextSwitch() {
        synchronized (stateLock) {
            if (Objects.requireNonNull(state) == State.RUNNING) {
                state = State.CONTEXT_SWITCH_REQUESTED;
            } else {
                throw new RuntimeException("Task is not ready for context switch");
            }
        }
    }

    public void requestKill() {
        synchronized (stateLock) {
            switch (state) {
                case READY_FOR_SCHEDULE, RUNNING, PAUSE_REQUESTED, PAUSED, CONTINUE_REQUESTED, CONTEXT_SWITCH_REQUESTED ->
                    state = State.KILL_REQUESTED;
                default -> throw new RuntimeException("Task is not ready for kill");
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
                case PAUSED -> state = State.CONTINUE_REQUESTED;
                case PAUSE_REQUESTED -> state = State.RUNNING;
                case RUNNING, CONTINUE_REQUESTED -> { }
                default -> throw new RuntimeException("Task is not ready for continue");
            }
        }
    }

    public void finish() {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                state = State.FINISHED;
                onFinishedOrKilled.accept(this);
                synchronized (waitForFinishLock) {
                    waitForFinishLock.notifyAll();
                }
            } else {
                throw new RuntimeException("Task can't finish");
            }
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
