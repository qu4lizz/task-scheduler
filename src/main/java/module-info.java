module qu4lizz.taskscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens qu4lizz.taskscheduler.gui to javafx.fxml;
    exports qu4lizz.taskscheduler.gui;
}