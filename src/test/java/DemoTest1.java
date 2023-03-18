import qu4lizz.taskscheduler.task.UserTask;

public class DemoTest1 extends UserTask {

    public DemoTest1(String name, int numOfThreads) {
        super(name, numOfThreads);
    }
    public DemoTest1(String name, int priority, int numOfThreads) {
        super(name, priority, numOfThreads);
    }
    public DemoTest1(String name, String startDate, int numOfThreads) {
        super(name, startDate, numOfThreads);
    }
    public DemoTest1(String name, int priority, String startDate, int numOfThreads) {
        super(name, priority, startDate, numOfThreads);
    }
    public DemoTest1(String name, String startDate, String endDate, int numOfThreads) {
        super(name, startDate, endDate, numOfThreads);
    }
    public DemoTest1(String name, int priority, String startDate, String endDate, int numOfThreads) {
        super(name, priority, startDate, endDate, numOfThreads);
    }
    public DemoTest1(String name, String startDate, int time, int numOfThreads) {
        super(name, startDate, time, numOfThreads);
    }
    public DemoTest1(String name, int priority, String startDate, int time, int numOfThreads) {
        super(name, priority, startDate, time, numOfThreads);
    }

    @Override
    public void execute() {
        System.out.println("Starting DemoTask1");
        for (int i = 0; i < 10; i++) {
            checks();

            setProgress((i + 1) / 10.0);

            System.out.println("DemoTask1: " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checks();
        }
    }
    private void checks() {
        checkForKill();
        checkForPause();
        checkForContextSwitch();
    }
}
