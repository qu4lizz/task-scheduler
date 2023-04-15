package qu4lizz.taskscheduler_implementation.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import qu4lizz.taskscheduler.scheduler.TaskScheduler;
import qu4lizz.taskscheduler.task.Task;
import qu4lizz.taskscheduler.task.UserTask;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.ResourceBundle;

public class GUIController implements Initializable {
    @FXML
    private GridPane root;
    @FXML
    private ListView<String> taskTypes;
    private final HashMap<String, Class<?>> nameToGUIClass = new HashMap<>();
    private TaskScheduler taskScheduler;
    private final RuntimeTypeAdapterFactory<UserTask> typeFactory = RuntimeTypeAdapterFactory.of(UserTask.class, "type");
    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            var input = Files.readAllLines(Paths.get("src/main/resources/qu4lizz/taskscheduler_implementation/gui/tasks.txt"));
            for (String line : input) {
                String[] split = line.split(" ");
                Class<?> classType = Class.forName(split[0]);
                typeFactory.registerSubtype((Class<? extends UserTask>) classType, split[1]);
                taskTypes.getItems().add(split[1]);
                nameToGUIClass.put(split[1], classType);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read tasks.txt");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find class");
        }
    }

    @FXML
    void addNewTaskOnMouseClicked(MouseEvent event) {
        var selected = taskTypes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertBox.display("Error", "Please select a task type");
            return;
        }
        Class<?> classType = nameToGUIClass.get(selected);
        try {
            // Open new window
            Constructor<?> constructor = classType.getConstructor();
            UserTaskGUI newTask = (UserTaskGUI)constructor.newInstance();

            Stage stage = newTask.getStage();
            stage.setTitle(GUI.TITLE);
            stage.setMinWidth(600);
            stage.setMinHeight(700);
            stage.getIcons().add(GUI.icon);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Add a task to list
            // think about mapping a task to hbox
            Task controllerTask = newTask.getTask().getTask();
            TaskHBox taskGUI = new TaskHBox(newTask.getTask().getName(), newTask.getTask().getTask());
            controllerTask.setOnProgressUpdate(taskGUI::updateProgress);
            taskGUI.setClearAction(this::clearTask);

            root.add(taskGUI, 0, root.getChildren().size());
            taskScheduler.addTask(newTask.getTask(), newTask.shouldStartImmediately());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearTask(TaskHBox task) {
        int ind = root.getChildren().indexOf(task);
        root.getChildren().remove(ind);
        // move all tasks that are below this one up
        for (int i = ind; i < root.getChildren().size(); i++) {
            root.getChildren().get(i).setTranslateY(-task.getHeight());
        }
    }

}
