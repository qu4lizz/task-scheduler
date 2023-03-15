package qu4lizz.taskscheduler.scheduler;

public class PreemptiveTaskScheduler extends PriorityTaskScheduler {
    public PreemptiveTaskScheduler(int numOfConcurrentTasks) {
        super(numOfConcurrentTasks);
    }

}
