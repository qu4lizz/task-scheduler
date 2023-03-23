package qu4lizz.taskscheduler.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FIFOTaskScheduler extends TaskScheduler {

    public FIFOTaskScheduler(int numOfConcurrentTasks) {
        super(numOfConcurrentTasks);
        waitingTasksToBeStarted = new ConcurrentLinkedQueue<>();
    }

}
