package qu4lizz.taskscheduler.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import qu4lizz.taskscheduler.scheduler.TaskScheduler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GUI extends Application {
    public static final String TITLE = "Task Scheduler";
    public static final String FXML_PATH = "main.fxml";
    public static final String ICON_PATH = "icons/main_icon.png";
    public static Image icon;
    public static String BACKGROUND_COLOR;
    public static String TEXT_COLOR;
    public static String BUTTON_COLOR;


    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        icon = new Image(GUI.class.getResource(ICON_PATH).toExternalForm());

        initializeColors();

        TaskScheduler taskScheduler = schedulerTypeScene();

        mainScene(stage, taskScheduler);

        stage.show();
    }

    private void initializeColors() throws IOException {
        var input = Files.readAllLines(Paths.get("src/main/resources/qu4lizz/taskscheduler/gui/colors.txt"));
        BACKGROUND_COLOR = input.get(0).split("=")[1];
        BUTTON_COLOR = input.get(1).split("=")[1];
        TEXT_COLOR = input.get(2).split("=")[1];
    }
    private void mainScene(Stage stage, TaskScheduler taskScheduler) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GUI.class.getResource(FXML_PATH));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.getIcons().add(icon);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        GUIController controller = fxmlLoader.getController();
        controller.setTaskScheduler(taskScheduler);
    }

    private TaskScheduler schedulerTypeScene() throws IOException {
            FXMLLoader fxmlLoaderEntry = new FXMLLoader(GUI.class.getResource("scheduler_type.fxml"));
            Scene sceneEntry = new Scene(fxmlLoaderEntry.load(), 600, 500);
            Stage stageEntry = new Stage();
            stageEntry.setScene(sceneEntry);
            stageEntry.initModality(Modality.APPLICATION_MODAL);
            stageEntry.setTitle(GUI.TITLE);
            stageEntry.setMinWidth(600);
            stageEntry.setMinHeight(500);
            stageEntry.getIcons().add(GUI.icon);
            SchedulerType controller = fxmlLoaderEntry.getController();
            controller.setStage(stageEntry);
            stageEntry.showAndWait();
            return controller.getTaskScheduler();
        }

}