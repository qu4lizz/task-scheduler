package qu4lizz.taskscheduler.tests;

import qu4lizz.taskscheduler.task.UserTask;

public class DemoTask3 extends UserTask {

    public DemoTask3(String name, int numOfThreads) {
        super(name, numOfThreads);
    }
    public DemoTask3(String name, int priority, int numOfThreads) {
        super(name, priority, numOfThreads);
    }
    public DemoTask3(String name, String startDate, int numOfThreads) {
        super(name, startDate, numOfThreads);
    }
    public DemoTask3(String name, int priority, String startDate, int numOfThreads) {
        super(name, priority, startDate, numOfThreads);
    }
    public DemoTask3(String name, String startDate, String endDate, int numOfThreads) {
        super(name, startDate, endDate, numOfThreads);
    }
    public DemoTask3(String name, int priority, String startDate, String endDate, int numOfThreads) {
        super(name, priority, startDate, endDate, numOfThreads);
    }
    public DemoTask3(String name, String startDate, int time, int numOfThreads) {
        super(name, startDate, time, numOfThreads);
    }
    public DemoTask3(String name, int priority, String startDate, int time, int numOfThreads) {
        super(name, priority, startDate, time, numOfThreads);
    }
    // executing 5s
    @Override
    public void execute() {
        System.out.println("Starting DemoTask1 " + getName());
        int numIterations = 50;
        tryLock("resource1");
        for (int i = 0; i < numIterations; i++) {
            checks();

            addProgress((i + 1) / (double)numIterations);

            System.out.println("DemoTask1 (" + getName() + "):" + i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            checks();
        }
        unlock("resource1");
        System.out.println("done");
    }
}
