package qu4lizz.taskscheduler.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import qu4lizz.taskscheduler.task.Task;


public class TaskHBox extends HBox {
    private final ImageView startIcon = new ImageView(GUI.startIcon);
    private final ImageView pauseIcon = new ImageView(GUI.pauseIcon);
    private final ImageView killIcon = new ImageView(GUI.stopIcon);
    private static final double ICON_OPACITY_LOW = 0.8;
    private static final double ICON_OPACITY_FULL = 1.0;

    private Task task;

    private ProgressBar progressBar;

    public void updateProgress(double progress) {
        if (progress < 0)
            progressBar.setProgress(0);
        else if (progress > 1)
            progressBar.setProgress(1);
        else
            progressBar.setProgress(progress);
    }

    public TaskHBox(String name, Task task) {
        this.task = task;

        setPrefHeight(50.0);
        setPrefWidth(610.0);

        Label label = new Label(name);
        label.setPrefHeight(30.0);
        label.setPrefWidth(229.0);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        label.setFont(new Font("IBM Plex Sans", 22.0));
        label.setStyle("-fx-text-fill: " + GUI.TEXT_COLOR + ";");
        HBox.setMargin(label, new Insets(10.0, 10.0, 10.0, 10.0));

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefHeight(31.0);
        progressBar.setPrefWidth(240.0);
        progressBar.setStyle("-fx-accent: " + GUI.BACKGROUND_COLOR + ";");
        HBox.setMargin(progressBar, new Insets(10.0, 10.0, 10.0, 10.0));

        processIcon(startIcon);
        startIcon.setOnMouseClicked(this::startOnMouseClicked);
        processIcon(pauseIcon);
        pauseIcon.setOnMouseClicked(this::pauseOnMouseClicked);
        processIcon(killIcon);
        killIcon.setOnMouseClicked(this::killOnMouseClicked);

        getChildren().addAll(label, progressBar, startIcon, pauseIcon, killIcon);
    }

    private void startOnMouseClicked(MouseEvent mouseEvent) {
        try {
            task.requestContinueOrStart();
        } catch (RuntimeException e) {
            AlertBox.display("Error", e.getMessage());
        }
        startIcon.setOpacity(ICON_OPACITY_LOW);
        pauseIcon.setOpacity(ICON_OPACITY_FULL);
    }

    private void pauseOnMouseClicked(MouseEvent mouseEvent) {
        try {
            task.requestPause();
        } catch (RuntimeException e) {
            AlertBox.display("Error", e.getMessage());
        }
        startIcon.setOpacity(ICON_OPACITY_FULL);
        pauseIcon.setOpacity(ICON_OPACITY_LOW);
    }

    private void killOnMouseClicked(MouseEvent mouseEvent) {
        try {
            task.requestKill();
        } catch (RuntimeException e) {
            AlertBox.display("Error", e.getMessage());
        }
        startIcon.setOpacity(ICON_OPACITY_LOW);
        pauseIcon.setOpacity(ICON_OPACITY_LOW);
        killIcon.setOpacity(ICON_OPACITY_LOW);
    }

    private static void processIcon(ImageView imageView) {
        imageView.setFitWidth(30.0);
        imageView.setFitHeight(30.0);
        imageView.setPreserveRatio(true);
        imageView.setPickOnBounds(true);
        HBox.setMargin(imageView, new Insets(10.0, 10.0, 10.0, 10.0));
    }
}
