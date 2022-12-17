package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.task.UserTask;

import java.util.concurrent.PriorityBlockingQueue;

public class PriorityTaskScheduler extends TaskScheduler {

    public PriorityTaskScheduler(int noOfTasks) {
        super(noOfTasks);
        nonActiveTasks = new PriorityBlockingQueue<>(noOfTasks, (x, y) ->
                Integer.compare(y.getPriority(), x.getPriority()));
    }


}
