package qu4lizz.taskscheduler.task;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;

import java.util.Objects;

public class Task {
    private State state = State.READY_FOR_SCHEDULE;
    private UserTask userTask;
    private final Object stateLock = new Object();
    private final Thread thread;
    private Action onFinished;
    private Action onPaused;
    private Action onContinue;
    private final Object pauseLock = new Object();
    private final Object waitForFinishLock = new Object();

    public interface Action {
        void act(UserTask task) throws InvalidRequestException;
    }
    enum State {
        READY_FOR_SCHEDULE, RUNNING, PAUSE_REQUESTED, PAUSED, CONTINUE_REQUESTED, FINISHED, KILLED
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
    public UserTask getUserTask() {
        return userTask;
    }
    public Action getOnPaused() { return onPaused; }
    public void setActions(Action onContinue, Action onFinished, Action onPaused) {
        this.onContinue = onContinue;
        this.onFinished = onFinished;
        this.onPaused = onPaused;
    }
    public Object getPauseLock() { return pauseLock; }
    public Object getWaitForFinishLock() { return waitForFinishLock; }

    public void start() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case READY_FOR_SCHEDULE -> {
                    state = State.RUNNING;
                    thread.start();
                }
                case CONTINUE_REQUESTED -> {
                    state = State.RUNNING;
                    onContinue.act(userTask);
                    synchronized (pauseLock) {
                        pauseLock.notify();
                    }
                }
                default -> throw new InvalidRequestException("Task is not ready for start");
            }
        }
    }

    public void requestPause() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case RUNNING -> state = State.PAUSE_REQUESTED;
                case CONTINUE_REQUESTED -> state = State.PAUSED;
                case PAUSED, PAUSE_REQUESTED -> { }
                default -> throw new InvalidRequestException("Task is not ready for pause");
            }
        }
    }

    public void requestKill() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case READY_FOR_SCHEDULE, RUNNING, PAUSE_REQUESTED, PAUSED, CONTINUE_REQUESTED -> {
                    state = State.KILLED;
                    onFinished.act(userTask);
                }
                default -> throw new InvalidRequestException("Task is not ready for kill");
            }
        }
    }

    public void requestContinue() throws InvalidRequestException {
        synchronized (stateLock) {
            switch (state) {
                case PAUSED -> state = State.CONTINUE_REQUESTED;
                case PAUSE_REQUESTED -> state = State.RUNNING;
                case RUNNING, CONTINUE_REQUESTED -> { }
                default -> throw new InvalidRequestException("Task is not ready for continue");
            }
        }
    }

    public void finish() throws InvalidRequestException {
        synchronized (stateLock) {
            if (Objects.requireNonNull(state) == State.RUNNING) {
                state = State.FINISHED;
                onFinished.act(userTask);
                synchronized (waitForFinishLock) {
                    waitForFinishLock.notifyAll();
                }
            } else {
                throw new IllegalStateException("Task can't finish");
            }
        }
    }
}
