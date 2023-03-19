package qu4lizz.taskscheduler.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class AlertBox {
    public static void display(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(350);
        window.setMinHeight(200);
        window.getIcons().add(GUI.icon);

        Label label = new Label(message);
        label.setTextFill(Color.web(GUI.TEXT_COLOR));
        label.setFont(Font.font("IBM Plex Serif Medm", FontPosture.REGULAR, 20));
        label.setWrapText(true);
        label.setMaxWidth(300);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);

        Button closeButton = new Button("OK");
        closeButton.setOnAction(e -> window.close());
        closeButton.setStyle("-fx-background-color: " + GUI.BUTTON_COLOR);
        closeButton.setTextFill(Color.web(GUI.TEXT_COLOR));
        closeButton.setFont(Font.font("IBM Plex Serif Medm", FontPosture.REGULAR, 22));

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: " + GUI.BACKGROUND_COLOR);
        layout.getChildren().addAll(label, closeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        scene.setFill(Color.web(GUI.BACKGROUND_COLOR));
        window.showAndWait();
    }
}
