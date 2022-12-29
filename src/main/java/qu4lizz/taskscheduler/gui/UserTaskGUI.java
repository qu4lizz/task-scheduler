package qu4lizz.taskscheduler.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import qu4lizz.taskscheduler.task.UserTask;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public abstract class UserTaskGUI implements Initializable {

    @FXML
    protected TextField endTimeField;

    @FXML
    protected TextField execTimeField;

    @FXML
    protected TextField nameField;

    @FXML
    protected TextField numberField;

    @FXML
    protected TextField priorityField;

    @FXML
    protected TextField startDateField;

    @FXML
    private GridPane inputGrid;
    private UserTask task;

    public UserTaskGUI(UserTaskGUI userTaskGUI) {
        this.nameField.setText(userTaskGUI.nameField.getText());
        this.priorityField.setText(userTaskGUI.priorityField.getText());
        this.numberField.setText(userTaskGUI.numberField.getText());
        this.startDateField.setText(userTaskGUI.startDateField.getText());
        this.endTimeField.setText(userTaskGUI.endTimeField.getText());
        this.execTimeField.setText(userTaskGUI.execTimeField.getText());
        this.inputGrid = userTaskGUI.inputGrid;
        this.task = userTaskGUI.task;
    }

    public GridPane getInputGrid () { return inputGrid; }

    public UserTask getTask () {
        return task;
    }

    public void setTask (UserTask task) {
        this.task = task;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for(TextField textField : Arrays.asList(endTimeField, startDateField, priorityField, numberField, nameField, execTimeField)) {
            textField.setStyle("-fx-text-fill" + GUI.TEXT_COLOR + "; -fx-background-color:" + GUI.BACKGROUND_COLOR
                    + "; -fx-border-color:" + GUI.TEXT_COLOR + ";");
        }
    }

    protected final void addNewInput(Label label, Control control) {
        inputGrid.add(label, 0, inputGrid.getRowCount());
        inputGrid.add(control, 1, inputGrid.getRowCount() - 1);
        label.setStyle("-fx-text-fill:" + GUI.TEXT_COLOR + ";" + "-fx-font-size: 22px;" + "-fx-font-family: 'IBM Plex Serif';" +
                "-fx-text-alignment: center; -fx-alignment: center;");
        control.setStyle("-fx-text-fill" + GUI.TEXT_COLOR + "; -fx-background-color:" + GUI.BACKGROUND_COLOR
                + "; -fx-border-color:" + GUI.TEXT_COLOR + ";");

        // TODO: Resize the window to fit the new input
    }

    /**
     *
     * Shall be override in child classes by calling:
     * if (validInput()) {
     *             switch (getConstructorType()) {
     *                 case NAME_NUM -> {
     *                 }
     *                 case NAME_PRI_NUM -> {
     *                 }
     *                 case NAME_START_NUM -> {
     *                 }
     *                 case NAME_PRI_START_NUM -> {
     *                 }
     *                 case NAME_START_END_NUM -> {
     *                 }
     *                 case NAME_PRI_START_END_NUM -> {
     *                 }
     *                 case NAME_START_TIME_NUM -> {
     *                 }
     *                 case NAME_PRI_START_TIME_NUM -> {
     *                 }
     *             }
     * }
     */
    @FXML
    protected abstract void createOnMouseClick (MouseEvent event) throws IOException, InterruptedException;

    protected boolean validInput() {
        if (nameField.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter the name of the task");
            return false;
        } else if (numberField.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter the number of the task");
            return false;
        } else if ((!execTimeField.getText().isEmpty() || endTimeField.getText().isEmpty()) && startDateField.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter the start date of the task");
            return false;
        }
        return true;
    }

    public BaseConstructor getConstructorType() {
        if (checkHelper(true, true, true, true)) {
            return BaseConstructor.NAME_NUM;
        } else if (checkHelper(true, true, false, true)) {
            return BaseConstructor.NAME_PRI_NUM;
        } else if (checkHelper(false, true, true, true)) {
            return BaseConstructor.NAME_START_NUM;
        } else if (checkHelper(false, true, false, true)) {
            return BaseConstructor.NAME_PRI_START_NUM;
        } else if (checkHelper(false, false, true, true)) {
            return BaseConstructor.NAME_START_END_NUM;
        } else if (checkHelper(false, false, false, true)) {
            return BaseConstructor.NAME_PRI_START_END_NUM;
        } else if (checkHelper(false, true, true, false)) {
            return BaseConstructor.NAME_START_TIME_NUM;
        } else if (checkHelper(false, true, false, false)) {
            return BaseConstructor.NAME_PRI_START_TIME_NUM;
        } else {
            throw new RuntimeException("This should never happen");
        }

    }

    private boolean checkHelper (boolean start, boolean end, boolean pri, boolean time) {
        start = !start ? !startDateField.getText().isEmpty() : startDateField.getText().isEmpty();
        end = !end ? !endTimeField.getText().isEmpty() : endTimeField.getText().isEmpty();
        pri = !pri ? !priorityField.getText().isEmpty() : priorityField.getText().isEmpty();
        time = !time ? !execTimeField.getText().isEmpty() : execTimeField.getText().isEmpty();
        return start && end && pri && time;
    }

    public enum BaseConstructor {
        NAME_NUM,
        NAME_PRI_NUM,
        NAME_START_NUM,
        NAME_PRI_START_NUM,
        NAME_START_END_NUM,
        NAME_PRI_START_END_NUM,
        NAME_START_TIME_NUM,
        NAME_PRI_START_TIME_NUM,
    }
}