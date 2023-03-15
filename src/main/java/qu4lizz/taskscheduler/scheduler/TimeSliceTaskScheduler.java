package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.task.Task;

import java.util.Timer;
import java.util.TimerTask;

public class TimeSliceTaskScheduler extends FIFOTaskScheduler {
    private final int timeSlice;

    public TimeSliceTaskScheduler(int numOfConcurrentTasks, int timeSlice) {
        super(numOfConcurrentTasks);
        this.timeSlice = timeSlice;
    }

    @Override
    protected void startTask(Task task) throws InvalidRequestException {
        super.startTask(task);
        timerStart(task);
    }

    private void timerStart(Task task) {
        int timeBeforeContextSwitch = timeSlice * task.getPriority();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (task.getState() == Task.State.RUNNING) {
                    try {
                        task.requestContextSwitch();
                    } catch (InvalidRequestException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        timer.schedule(timerTask, timeBeforeContextSwitch);
    }
}
