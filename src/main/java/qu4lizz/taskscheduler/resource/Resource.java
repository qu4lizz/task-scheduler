package qu4lizz.taskscheduler.resource;

import qu4lizz.taskscheduler.task.Task;
import qu4lizz.taskscheduler.utils.Graph;

import java.util.PriorityQueue;

public class Resource {
    private final Graph<Task> graph = new Graph<>();
    private final String id;
    private Integer holderPriority = null;
    private Task holder = null;
    private final PriorityQueue<Task> waitingQueue = new PriorityQueue<Task>((x, y) -> x.getPriority() - y.getPriority());
    private final Object lock = new Object();

    public Resource(String id) {
        this.id = id;
    }

    // Priority Ceiling Protocol
    public void tryLock(Task task) {
        boolean status = false;
        synchronized (lock) {
            if (this.holder != null) {
                graph.addTransition(task, holder);
                waitingQueue.add(task);

                if (task.getPriority() > holder.getPriority()) {
                    holderPriority = holder.getPriority();
                    holder.setPriority(task.getPriority());
                }
                status = true;
            }
            else {
                this.holder = task;
            }
        }
        if (status) {
            // stop executing while waiting for resource
            task.blockForResource();
            this.holder = task;
        }
    }

    public void unlock() {
        synchronized (lock) {
            // owner == null, priority changed
            if (holder != null) {
                if (this.holderPriority != null) {
                    this.holder.setPriority(this.holderPriority);
                    this.holderPriority = null;
                }
                this.holder = null;

                if (!waitingQueue.isEmpty()) {
                    Task task = waitingQueue.poll();
                    graph.removeNode(task);
                    // start executing again
                    task.unblockForResource();
                }
            }
        }
    }
}

