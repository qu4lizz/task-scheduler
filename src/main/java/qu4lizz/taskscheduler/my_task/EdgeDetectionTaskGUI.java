package qu4lizz.taskscheduler.my_task;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import qu4lizz.taskscheduler.gui.UserTaskGUI;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EdgeDetectionTaskGUI extends UserTaskGUI {
    private final Label imagesLabel = new Label("Images to process:");
    private final Button chooseImagesButton = new Button("Choose Images");
    private final Label outputLabel = new Label("Output directory:");
    private final Button chooseOutputButton = new Button("Choose Output Directory");
    private List<File> images;
    private File output;

    public EdgeDetectionTaskGUI() {
        addNewInput(imagesLabel, chooseImagesButton);
        addNewInput(outputLabel, chooseOutputButton);
        chooseImagesButton.setOnMouseClicked(this::chooseImagesOnMouseClicked);
        chooseOutputButton.setOnMouseClicked(this::chooseOutputOnMouseClicked);
    }

    private void chooseOutputOnMouseClicked(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Output Directory");
        output = directoryChooser.showDialog(chooseOutputButton.getScene().getWindow());
    }

    private void chooseImagesOnMouseClicked(MouseEvent evt) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        images = fileChooser.showOpenMultipleDialog(null);
    }

    @Override
    public void createOnMouseClick(MouseEvent event) throws IOException, InterruptedException {
        if (validInput()) {
            String[] imgSrc = (String[]) images.toArray();
            String outputPath = output.getAbsolutePath();
            switch (getConstructorType()) {
                case NAME_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), Integer.parseInt(numberField.getText()),
                            imgSrc, outputPath), startCheckbox.isSelected());
                case NAME_PRI_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), Integer.parseInt(priorityField.getText()),
                            Integer.parseInt(numberField.getText()), imgSrc, outputPath), startCheckbox.isSelected());
                case NAME_START_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), startDateField.getText(),
                            Integer.parseInt(numberField.getText()), imgSrc, outputPath), startCheckbox.isSelected());
                case NAME_PRI_START_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), Integer.parseInt(priorityField.getText()),
                            startDateField.getText(), Integer.parseInt(numberField.getText()),
                            imgSrc, outputPath), startCheckbox.isSelected());
                case NAME_START_END_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), startDateField.getText(),
                            endTimeField.getText(), Integer.parseInt(numberField.getText()),
                            imgSrc, outputPath), startCheckbox.isSelected());
                case NAME_PRI_START_END_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), Integer.parseInt(priorityField.getText()),
                            startDateField.getText(), endTimeField.getText(), Integer.parseInt(numberField.getText()),
                            imgSrc, outputPath), startCheckbox.isSelected());
                case NAME_START_TIME_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), startDateField.getText(),
                            Integer.parseInt(numberField.getText()), Integer.parseInt(numberField.getText()),
                            imgSrc, outputPath), startCheckbox.isSelected());
                case NAME_PRI_START_TIME_NUM ->
                    setTaskSpecification(new EdgeDetectionTask(nameField.getText(), Integer.parseInt(priorityField.getText()),
                            startDateField.getText(), Integer.parseInt(numberField.getText()), Integer.parseInt(numberField.getText()),
                            imgSrc, outputPath), startCheckbox.isSelected());
            }
        }
    }
}
