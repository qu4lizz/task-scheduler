package qu4lizz.taskscheduler_implementation.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import qu4lizz.taskscheduler.task.UserTask;

import java.io.IOException;
import java.util.Arrays;

public abstract class UserTaskGUI {
    private final Font labelFont = new Font("IBM Plex Sans", 22);
    private final Font buttonFont = Font.font("IBM Plex Sans Thai SmBld", 22.0);
    private final AnchorPane anchorPane;
    private Label titleLabel = new Label("Task Specification");
    private Label nameLabel = new Label("Name of task:");
    private Label numberOfThreadsLabel = new Label("Number of threads:");
    private Label priorityLabel = new Label("Priority (optional):");
    private Label startDateLabel = new Label("Start date (optional):");
    private Label endDateLabel = new Label("End date (optional):");
    private Label executionTimeLabel = new Label("Execution time (optional):");
    private Label startImmediatelyLabel = new Label("Start immediately:");

    protected TextField endTimeField = new TextField();
    protected TextField execTimeField = new TextField();
    protected TextField nameField = new TextField();
    protected TextField numberField = new TextField();
    protected TextField priorityField = new TextField();
    protected TextField startDateField = new TextField();
    protected CheckBox startCheckbox = new CheckBox();

    private GridPane grid;
    private UserTask task;
    private boolean startImmediately;
    private Scene scene;
    private Stage stage;

    public UserTaskGUI() {
        anchorPane = new AnchorPane();
        anchorPane.setMinSize(700, 600);
        anchorPane.setPrefSize(700, 600);
        anchorPane.setStyle("-fx-background-color:" + GUI.BACKGROUND_COLOR + ";");

        titleLabel = new Label("Task Specification");
        Font titleFont = new Font("IBM Plex Sans SmBld", 35);
        titleLabel.setFont(titleFont);
        titleLabel.setStyle("-fx-text-fill:" + GUI.TEXT_COLOR + ";");
        titleLabel.setLayoutX(6);
        titleLabel.setLayoutY(14);
        titleLabel.setPrefSize(590, 37);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setUnderline(true);
        AnchorPane.setRightAnchor(titleLabel, 10.0);
        AnchorPane.setLeftAnchor(titleLabel, 10.0);

        initLabel(nameLabel);
        initLabel(numberOfThreadsLabel);
        initLabel(priorityLabel);
        initLabel(startDateLabel);
        initLabel(endDateLabel);
        initLabel(executionTimeLabel);
        initLabel(startImmediatelyLabel);

        for (TextField textField : Arrays.asList(endTimeField, startDateField, priorityField, numberField, nameField, execTimeField))
            initTextField(textField);


        grid = new GridPane();
        initGrid();
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(numberOfThreadsLabel, 0, 1);
        grid.add(numberField, 1, 1);
        grid.add(priorityLabel, 0, 2);
        grid.add(priorityField, 1, 2);
        grid.add(startDateLabel, 0, 3);
        grid.add(startDateField, 1, 3);
        grid.add(endDateLabel, 0, 4);
        grid.add(endTimeField, 1, 4);
        grid.add(executionTimeLabel, 0, 5);
        grid.add(execTimeField, 1, 5);
        grid.add(startImmediatelyLabel, 0, 6);
        grid.add(startCheckbox, 1, 6);

        Button button = new Button("Create");
        button.setAlignment(Pos.CENTER);
        button.setLayoutX(234.0);
        button.setLayoutY(559.0);
        button.setMnemonicParsing(false);
        button.setOnMouseClicked(e -> {
            try {
                mouseAction(e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        button.setPrefHeight(62.0);
        button.setPrefWidth(134.0);

        AnchorPane.setLeftAnchor(button, 234.0);
        AnchorPane.setRightAnchor(button, 233.0);

        button.setFont(buttonFont);
        button.setStyle("-fx-background-color: #3b484d;\n" +
                "    -fx-background-radius: 30px;\n" +
                "    -fx-border-color: #674448;\n" +
                "    -fx-border-width: 3;\n" +
                "    -fx-border-radius: 25px;\n" +
                "    -fx-text-fill: #fc8789;");

        anchorPane.getChildren().addAll(titleLabel, grid, button);

        scene = new Scene(anchorPane);
        stage = new Stage();
        stage.setScene(scene);
    }

    public GridPane getGrid() { return grid; }

    public Stage getStage() { return stage; }

    public void setTaskSpecification(UserTask task, boolean startImmediately) {
        this.task = task;
        this.startImmediately = startImmediately;
    }
    public boolean shouldStartImmediately() { return startImmediately; }
    public UserTask getTask() { return task; }

    protected final void addNewInput(Label label, Control control) {
        initLabel(label);
        if (control instanceof TextField)
            initTextField((TextField) control);

        else {
            control.setMinSize(288.5, 30.0);
            control.setPrefSize(288.5, 30.0);
            control.setStyle("-fx-text-fill:" + GUI.TEXT_COLOR + "; -fx-background-color:" + GUI.BACKGROUND_COLOR  + "; -fx-border-color:" + GUI.TEXT_COLOR + ";");
        }
        grid.add(label, 0, grid.getRowCount());
        grid.add(control, 1, grid.getRowCount() - 1);
    }

    /**
     *
     * Shall be overridden in child classes by calling:
     *          if (validInput()) {
     *             switch (getConstructorType()) {
     *                 case NAME_NUM -> {
     *                      setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText())), startCheckbox.isSelected());
     *                 }
     *                 case NAME_PRI_NUM -> {
     *                      setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText()), Integer.parseInt(priorityField.getText())), startCheckbox.isSelected());
     *                 }
     *                 case NAME_START_NUM -> {
     *                      setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText()), startDateField.getText()), startCheckbox.isSelected());
     *                 }
     *                 case NAME_PRI_START_NUM -> {
     *                      setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText()), Integer.parseInt(priorityField.getText()), startDateField.getText()), startCheckbox.isSelected());
     *                 }
     *                 case NAME_START_END_NUM -> {
     *                   setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText()), startDateField.getText(), endTimeField.getText()), startCheckbox.isSelected());
     *                 }
     *                 case NAME_PRI_START_END_NUM -> {
     *                   setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText()), Integer.parseInt(priorityField.getText()), startDateField.getText(), endTimeField.getText()), startCheckbox.isSelected());
     *                 }
     *                 case NAME_START_TIME_NUM -> {
     *                  setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText()), startDateField.getText(), execTimeField.getText()), startCheckbox.isSelected());
     *                 }
     *                 case NAME_PRI_START_TIME_NUM -> {
     *                      setTask(new UserTask(nameField.getText(), Integer.parseInt(numberField.getText()), Integer.parseInt(priorityField.getText()), startDateField.getText(), execTimeField.getText()), startCheckbox.isSelected());
     *                 }
     *             }
     *          }
     */
    protected abstract void createOnMouseClick(MouseEvent event) throws IOException, InterruptedException;

    public final void mouseAction(MouseEvent event) throws IOException, InterruptedException {
        createOnMouseClick(event);
        stage.close();
    }
    protected boolean validInput() {
        if (nameField.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter the name of the task");
            return false;
        } else if (numberField.getText().isEmpty()) {
            AlertBox.display("Error", "Please enter the number of the task");
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

    private void initGrid() {
        grid.setAlignment(Pos.CENTER);
        grid.setLayoutX(30.0);
        grid.setLayoutY(80.0);
        grid.setPrefSize(580.0, 244.0);
        AnchorPane.setLeftAnchor(grid, 10.0);
        AnchorPane.setRightAnchor(grid, 10.0);
    }
    private void initLabel(Label label) {
        label.setAlignment(Pos.CENTER);
        label.setPrefSize(288.5, 30.0);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setTextFill(Color.web(GUI.TEXT_COLOR));
        label.setFont(labelFont);
    }
    private void initTextField(TextField textField) {
        textField.setPrefSize(288.5, 30.0);
        textField.setStyle("-fx-text-inner-color:" + GUI.TEXT_COLOR + "; " + "-fx-text-fill:" + GUI.TEXT_COLOR + "; -fx-background-color:" + GUI.BUTTON_COLOR  + "; -fx-border-color:" + GUI.TEXT_COLOR + ";");
        textField.setFont(labelFont);
        textField.setAlignment(Pos.CENTER);
    }
}