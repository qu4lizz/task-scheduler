package qu4lizz.taskscheduler.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
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
    private ListView<UserTask> tasks;

    @FXML
    private ListView<String> taskTypes;
    private HashMap<String, Class<?>> nameToGUIClass = new HashMap<>();

    public ListView<UserTask> getTasks () { return tasks; }

    private final RuntimeTypeAdapterFactory<UserTask> typeFactory = RuntimeTypeAdapterFactory.of(UserTask.class, "type");
    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            var input = Files.readAllLines(Paths.get("src/main/resources/qu4lizz/taskscheduler/gui/tasks.txt"));
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
            FXMLLoader fxmlLoader = new FXMLLoader(GUI.class.getResource("new_task.fxml"));
            UserTask task = fxmlLoader.getController();
            Constructor<?> constructor = classType.getConstructor(UserTask.class);
            UserTask newTask = (UserTask) constructor.newInstance(task);
            Scene scene = new Scene(newTask, 600, 700);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(GUI.TITLE);
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.getIcons().add(GUI.icon);
            SchedulerType controller = fxmlLoader.getController();
            controller.setStage(stage);
            stage.show();
            var taskGUI = (UserTaskGUI) Class.forName(classType.getName()).newInstance();

        } catch (Exception e) {
            throw new RuntimeException("Could not create task");
        }
    }

}
