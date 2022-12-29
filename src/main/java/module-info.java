module qu4lizz.taskscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    requires gson.extras;
    requires org.catacombae.jfuse.types.fuse;

    opens qu4lizz.taskscheduler.gui to javafx.fxml;
    exports qu4lizz.taskscheduler.gui;
    exports qu4lizz.taskscheduler.my_task;
    opens qu4lizz.taskscheduler.my_task to javafx.fxml;
}