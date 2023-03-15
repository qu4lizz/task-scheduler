package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.exceptions.InvalidRequestException;
import qu4lizz.taskscheduler.task.Task;
import qu4lizz.taskscheduler.task.UserTask;
import qu4lizz.taskscheduler.utils.ConcurrentPriorityQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimeSliceTaskScheduler extends FIFOTaskScheduler {
    private final int timeSlice;
    private Thread controlThread;

    public TimeSliceTaskScheduler(int numOfConcurrentTasks, int timeSlice) {
        super(numOfConcurrentTasks);
        this.timeSlice = timeSlice;
    }
    
    @Override
    public void addTask(Task task) throws InvalidRequestException {
        super.addTask(task);
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

    @Override
    protected void addTaskActions(Task task) {
        task.setActions(this::handleTaskContinue, this::handleTaskFinishedOrKilled, this::handleTaskPaused, this::handleTaskContextSwitch);
    }
    
    private void control() {
        while (enabled.get()) {

        }
    }
}
