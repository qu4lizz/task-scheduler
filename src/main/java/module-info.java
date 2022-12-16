module qu4lizz.taskscheduler {
    requires javafx.controls;
    requires javafx.fxml;


    opens qu4lizz.taskscheduler.gui to javafx.fxml;
    exports qu4lizz.taskscheduler.gui;
}