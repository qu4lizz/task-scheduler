package qu4lizz.taskscheduler.scheduler;

import qu4lizz.taskscheduler.task.Task;

public class PreemptiveTaskScheduler extends PriorityTaskScheduler {
    public PreemptiveTaskScheduler(int numOfConcurrentTasks) {
        super(numOfConcurrentTasks);
    }

    @Override
    protected boolean taskCanBeStarted(Task task) {
        if (super.taskCanBeStarted(task))
            return true;
        if (activeTasks.size() == maxTasks.get()) {
            int lowestPriority = -1; // highest priority is 0
            Task lowestPriorityTask = null;
            for(var activeTask : activeTasks)
                if (activeTask.getPriority() > lowestPriority) {
                    lowestPriority = activeTask.getPriority();
                    lowestPriorityTask = activeTask;
                }
            if (task.getPriority() < lowestPriority)
                return true;
        }
        return false;
    }

    protected void startTask(Task task) {
        if (activeTasks.size() < maxTasks.get()) {
            super.startTask(task);
        }
        else if (activeTasks.size() == maxTasks.get()) {
            int lowestPriority = -1; // highest priority is 0
            Task lowestPriorityTask = null;
            for(var activeTask : activeTasks)
                if (activeTask.getPriority() > lowestPriority) {
                    lowestPriority = activeTask.getPriority();
                    lowestPriorityTask = activeTask;
                }

            waitingTasksToBeStarted.add(task);
            if (task.getPriority() < lowestPriority) {
                lowestPriorityTask.requestContextSwitch();
                // TODO: check if this is it, will it stop lowestPriorityTask and start task
            }
        }
        else {
            throw new RuntimeException("This should never happen");
        }
    }
}
