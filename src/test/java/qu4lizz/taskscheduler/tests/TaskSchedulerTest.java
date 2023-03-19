package qu4lizz.taskscheduler.tests;

import org.junit.jupiter.api.Test;
import qu4lizz.taskscheduler.resource.Resource;
import qu4lizz.taskscheduler.scheduler.FIFOTaskScheduler;
import qu4lizz.taskscheduler.scheduler.PreemptiveTaskScheduler;
import qu4lizz.taskscheduler.task.Task;
import qu4lizz.taskscheduler.task.UserTask;
import qu4lizz.taskscheduler.utils.Utils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskSchedulerTest {

    @Test
    void scheduling() {
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(1);

        UserTask task1 = new DemoTask1("demo1", 1);
        UserTask task2 = new DemoTask1("demo2", 1);

        fifoTaskScheduler.addTask(task1, false);
        fifoTaskScheduler.addTask(task2, true);

        assertSame(task1.getState(), Task.State.NOT_SCHEDULED);
        assertSame(task2.getState(), Task.State.RUNNING);
    }

    @Test
    void waitForFinish() throws InterruptedException {
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(1);

        UserTask task1 = new DemoTask1("demo1", 1);
        UserTask task2 = new DemoTask1("demo2", 1);

        fifoTaskScheduler.addTask(task1, true);
        fifoTaskScheduler.addTask(task2, true);

        assertSame(task2.getState(), Task.State.READY_FOR_SCHEDULE);

        task1.getTask().waitForFinish();

        Thread.sleep(300);

        assertSame(task2.getState(), Task.State.RUNNING);

    }

    @Test
    void pauseAndContinue() throws InterruptedException {
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(1);

        UserTask task1 = new DemoTask1("demo1", 1);

        fifoTaskScheduler.addTask(task1, true);

        task1.getTask().requestPause();
        Thread.sleep(200);
        System.out.println(task1.getState());
        assertSame(task1.getState(), Task.State.PAUSED);

        task1.getTask().requestContinueOrStart();
        Thread.sleep(300);
        System.out.println(task1.getState());
        assertSame(task1.getState(), Task.State.RUNNING);
    }

    @Test
    void kill() throws InterruptedException{
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(1);

        UserTask task1 = new DemoTask1("demo1", 1);

        fifoTaskScheduler.addTask(task1, true);

        task1.getTask().requestKill();
        Thread.sleep(300);
        assertSame(task1.getState(), Task.State.KILLED);
    }

    @Test
    void priorityPreemptiveScheduler() throws InterruptedException {
        PreemptiveTaskScheduler preemptiveTaskScheduler = new PreemptiveTaskScheduler(1);

        UserTask task1 = new DemoTask1("demo1", 5, 1);
        UserTask task2 = new DemoTask1("demo2", 2, 1);

        preemptiveTaskScheduler.addTask(task1, true);
        Thread.sleep(100);

        assertSame(task1.getState(), Task.State.RUNNING);
        assertSame(task2.getState(), Task.State.READY_FOR_SCHEDULE);

        preemptiveTaskScheduler.addTask(task2, true);
        Thread.sleep(400);

        assertSame(task1.getState(), Task.State.CONTEXT_SWITCHED);
        assertSame(task2.getState(), Task.State.RUNNING);
    }

    @Test
    void startDate() throws InterruptedException {
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(1);

        String currentDateAndTime = Utils.getCurrentDateAndTime();
        String startDate = Utils.calculateEndDate(currentDateAndTime, 5);

        UserTask task1 = new DemoTask1("demo1", startDate, 1);

        fifoTaskScheduler.addTask(task1, true);

        assertSame(task1.getState(), Task.State.READY_FOR_SCHEDULE);
        Thread.sleep(5000);
        assertSame(task1.getState(), Task.State.RUNNING);
        Thread.sleep(2000);
        assertSame(task1.getState(), Task.State.FINISHED);
    }
    // TODO: Test when task has startDate but doesn't start immediately

    @Test
    void endDate() throws InterruptedException {
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(1);

        String currentDateAndTime = Utils.getCurrentDateAndTime();
        String endDate = Utils.calculateEndDate(currentDateAndTime, 3);

        UserTask task1 = new DemoTask2("demo1", currentDateAndTime, endDate, 1);

        fifoTaskScheduler.addTask(task1, true);

        Thread.sleep(500);
        assertSame(task1.getState(), Task.State.RUNNING);

        Thread.sleep(3000);
        System.out.println(task1.getState());
        assertSame(task1.getState(), Task.State.KILLED);
        assertTrue(fifoTaskScheduler.isEmpty());
    }

    @Test
    void executionTime() throws InterruptedException {
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(1);

        String currentDate = Utils.getCurrentDateAndTime();

        UserTask task1 = new DemoTask2("demo1", currentDate, 2, 2);

        fifoTaskScheduler.addTask(task1, true);

        Thread.sleep(500);
        assertSame(task1.getState(), Task.State.RUNNING);

        Thread.sleep(2000);
        assertSame(task1.getState(), Task.State.KILLED);
    }

    @Test
    void resourceTest() throws InterruptedException {
        FIFOTaskScheduler fifoTaskScheduler = new FIFOTaskScheduler(2);

        Resource resource = new Resource("resource1");
        fifoTaskScheduler.addResource(resource);

        UserTask task1 = new DemoTask3("demo1", 1, 5);
        UserTask task2 = new DemoTask3("demo2", 3, 5);

        fifoTaskScheduler.addTask(task1, true);
        fifoTaskScheduler.addTask(task2, true);

        Thread.sleep(500);
        assertSame(task1.getState(), Task.State.RUNNING);
        assertSame(task2.getState(), Task.State.WAITING_FOR_RESOURCE);

    }
}
