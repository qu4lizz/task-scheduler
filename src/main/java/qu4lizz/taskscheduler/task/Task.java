package qu4lizz.taskscheduler.task;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.utils.Utils;

public class Task {
    private State state = State.READY_FOR_SCHEDULE;
    private final UserTask userTask;
    private final Object stateLock = new Object();
    private final Thread thread;
    private Action onFinishedOrKilled;
    private Action onPaused;
    private Action onContinue;
    private Action onContextSwitch;
    private Action onStarted;

    private final Object pauseLock = new Object();
    private final Object waitForFinishLock = new Object();
    private final Object finishLock = new Object();
    public final Object contextSwitchLock = new Object();

    public interface Action {
        void act(Task task) throws InvalidRequestException;
    }
    public enum State {
        NOT_SCHEDULED,
        READY_FOR_SCHEDULE,
        RUNNING,
        PAUSE_REQUESTED,
        PAUSED,
        CONTINUE_REQUESTED,
        CONTEXT_SWITCH_REQUESTED,
        CONTEXT_SWITCHED,
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
                try {
                    finish();
                } catch (InvalidRequestException ignore) { }
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
    public Object getStateLock() {
        return stateLock;
    }
    public Object getContextSwitchLock() { return contextSwitchLock; }
    public UserTask getUserTask() {
        return userTask;
    }
    public Action getOnPaused() { return onPaused; }
    public Action getOnFinishedOrKilled() { return onFinishedOrKilled; }
    public Action getOnContextSwitch() { return onContextSwitch; }
    public void setActions(Action onContinue, Action onFinished, Action onPaused, Action onContextSwitch, Action onStarted) {
        this.onContinue = onContinue;
        this.onFinishedOrKilled = onFinished;
        this.onPaused = onPaused;
        this.onContextSwitch = onContextSwitch;
        this.onStarted = onStarted;
    }
    public Object getPauseLock() { return pauseLock; }
    public Object getWaitForFinishLock() { return waitForFinishLock; }
    public Object getFinishLock() { return finishLock; }

    // method called by TaskScheduler, not by user
    public void start() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case READY_FOR_SCHEDULE -> {
                    state = State.RUNNING;
                    thread.start();
                }
                case CONTINUE_REQUESTED -> {
                    state = State.RUNNING;
                    onContinue.act(this);
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
                default -> throw new InvalidRequestException("Task is not ready for start");
            }
        }
    }

    public void requestPause() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                // TODO: what happens if task is in context switch?
                case RUNNING -> state = State.PAUSE_REQUESTED;
                case CONTINUE_REQUESTED -> state = State.PAUSED;
                case PAUSED, PAUSE_REQUESTED -> { }
                default -> throw new InvalidRequestException("Task is not ready for pause");
            }
        }
    }

    public void requestContextSwitch() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case RUNNING -> state = State.CONTEXT_SWITCH_REQUESTED;
                default -> throw new InvalidRequestException("Task is not ready for context switch");
            }
        }
    }

    public void requestKill() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case READY_FOR_SCHEDULE, RUNNING, PAUSE_REQUESTED, PAUSED, CONTINUE_REQUESTED, CONTEXT_SWITCH_REQUESTED ->
                    state = State.KILL_REQUESTED;
                default -> throw new InvalidRequestException("Task is not ready for kill");
            }
        }
    }

    public void requestContinueOrStart() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case NOT_SCHEDULED -> {
                    state = State.READY_FOR_SCHEDULE;
                    onStarted.act(this);
                }
                case PAUSED -> state = State.CONTINUE_REQUESTED;
                case PAUSE_REQUESTED -> state = State.RUNNING;
                case RUNNING, CONTINUE_REQUESTED -> { }
                default -> throw new InvalidRequestException("Task is not ready for continue");
            }
        }
    }

    public void finish() throws InvalidRequestException {
        synchronized (stateLock) {
            if (state == State.RUNNING) {
                state = State.FINISHED;
                onFinishedOrKilled.act(this);
                synchronized (waitForFinishLock) {
                    waitForFinishLock.notifyAll();
                }
            } else {
                throw new IllegalStateException("Task can't finish");
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
