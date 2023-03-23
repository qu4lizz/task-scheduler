module qu4lizz.taskscheduler_implementation.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    requires gson.extras;
    requires qu4lizz.taskscheduler;

    opens qu4lizz.taskscheduler_implementation.gui to javafx.fxml;
    exports qu4lizz.taskscheduler_implementation.gui;
    exports qu4lizz.taskscheduler_implementation.my_task;
    opens qu4lizz.taskscheduler_implementation.my_task to javafx.fxml;
}