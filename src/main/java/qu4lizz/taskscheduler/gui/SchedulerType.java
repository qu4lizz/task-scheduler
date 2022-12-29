package qu4lizz.taskscheduler.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import qu4lizz.taskscheduler.scheduler.FIFOTaskScheduler;
import qu4lizz.taskscheduler.scheduler.PriorityTaskScheduler;
import qu4lizz.taskscheduler.scheduler.TaskScheduler;
import java.net.URL;
import java.util.ResourceBundle;

public class SchedulerType implements Initializable {

    @FXML
    private ImageView fifoImg;

    @FXML
    private ImageView priorityImg;

    @FXML
    private TextField numOfTasks;
    private TaskScheduler taskScheduler;
    private int numOfTasksInt;
    private Stage stage;

    public void setStage(Stage stage) {
    this.stage = stage;
}
    public TaskScheduler getTaskScheduler () {
        return taskScheduler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        numOfTasks.setStyle("-fx-text-fill:#fc8789; -fx-background-color:#3b484d; -fx-border-color:#fc8789;");
    }

    @FXML
    void fifoOnMouseClicked(MouseEvent event) {
        if (checks()) {
            taskScheduler = new FIFOTaskScheduler(numOfTasksInt);
            stage.close();
        }
    }

    @FXML
    void priorityOnMouseClicked(MouseEvent event) {
        if (checks()) {
            taskScheduler = new PriorityTaskScheduler(numOfTasksInt);
            stage.close();
        }
    }
    private boolean checks () {
        if (numOfTasks.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter the number of tasks");
            return false;
        }
        else if (Integer.parseInt(numOfTasks.getText()) < 1) {
            AlertBox.display("Error", "Please enter a number greater than 0");
            return false;
        }
        try {
            numOfTasksInt = Integer.parseInt(numOfTasks.getText());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}