package qu4lizz.taskscheduler.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import qu4lizz.taskscheduler.scheduler.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SchedulerType implements Initializable {
    @FXML
    private ChoiceBox<String> schedulerType;
    @FXML
    private TextField numOfTasks;
    @FXML
    private TextField timeSliceField;
    @FXML
    private Label timeSliceLabel;
    String[] schedulerOptions = {"FIFO", "Priority", "Priority Based Round Robin", "Preemptive Priority"};
    private TaskScheduler taskScheduler;
    private int numOfTasksInt;
    private int timeSliceInt;
    private Stage stage;
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(windowEvent -> {
            Platform.exit();
            System.exit(0);
        });
    }
    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        schedulerType.getItems().addAll(schedulerOptions);
        schedulerType.setOnAction(this::getSchedulerType);
        numOfTasks.setStyle("-fx-text-fill:#fc8789; -fx-background-color:#3b484d; -fx-border-color:#fc8789;");
        timeSliceField.setStyle("-fx-text-fill:#fc8789; -fx-background-color:#3b484d; -fx-border-color:#fc8789;");
        timeSliceField.setVisible(false);
        timeSliceLabel.setVisible(false);
    }

    @FXML
    void startTaskSchedulerOnMouseClicked(MouseEvent event) { // this and closeOnMouseClicked
        if (checks()) {
            switch (schedulerType.getValue()) {
                case "FIFO" -> taskScheduler = new FIFOTaskScheduler(numOfTasksInt);
                case "Priority" -> taskScheduler = new PriorityTaskScheduler(numOfTasksInt);
                case "Priority Based Round Robin" ->
                        taskScheduler = new RoundRobinPriorityBasedScheduler(numOfTasksInt, timeSliceInt);
                case "Preemptive Priority" -> taskScheduler = new PreemptiveTaskScheduler(numOfTasksInt);
            }
            stage.close();
        }
    }

    private void getSchedulerType(ActionEvent event) {
        switch (schedulerType.getValue()) {
            case "FIFO", "Priority", "Preemptive Priority" -> {
                timeSliceField.setVisible(false);
                timeSliceLabel.setVisible(false);
            }
            case "Priority Based Round Robin" -> {
                timeSliceField.setVisible(true);
                timeSliceLabel.setVisible(true);
            }
        }
    }

    private boolean checks() {
        if (numOfTasks.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter the number of tasks");
            return false;
        }
        else if (!numOfTasks.getText().matches("[0-9]+")) {
            AlertBox.display("Error", "Please enter a valid number of tasks");
            return false;
        }
        else if (Integer.parseInt(numOfTasks.getText()) < 1) {
            AlertBox.display("Error", "Please enter a number greater than 0");
            return false;
        }
        else if (schedulerType.getValue() == null) {
            AlertBox.display("Error", "Please select a scheduler type");
            return false;
        }
        else if (schedulerType.getValue().equals("Priority Based Round Robin") && timeSliceField.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter a time slice");
            return false;
        }
        else if (schedulerType.getValue().equals("Priority Based Round Robin") && !timeSliceField.getText().matches("[0-9]+")) {
            AlertBox.display("Error", "Please enter a valid time slice");
            return false;
        }
        else if (schedulerType.getValue().equals("Priority Based Round Robin") && Integer.parseInt(timeSliceField.getText()) < 1) {
            AlertBox.display("Error", "Please enter a time slice greater than 0");
            return false;
        }
        try {
            numOfTasksInt = Integer.parseInt(numOfTasks.getText());
            if (schedulerType.getValue().equals("Priority Based Round Robin"))
                timeSliceInt = Integer.parseInt(timeSliceField.getText());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}