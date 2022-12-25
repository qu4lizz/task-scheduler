package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.utils.ConcurrentPriorityQueue;

public class PriorityTaskScheduler extends TaskScheduler {

    public PriorityTaskScheduler(int noOfTasks) {
        super(noOfTasks);
        nonActiveTasks = new ConcurrentPriorityQueue<>((x, y) -> y.getPriority() - x.getPriority());
    }


}
