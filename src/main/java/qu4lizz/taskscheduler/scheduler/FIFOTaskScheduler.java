package qu4lizz.taskscheduler.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FIFOTaskScheduler extends TaskScheduler {

    public FIFOTaskScheduler(int noOfTasks) {
        super(noOfTasks);
        nonActiveTasks = new ConcurrentLinkedQueue<>();
    }

}
