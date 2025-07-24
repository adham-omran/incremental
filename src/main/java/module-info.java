module com.adham_omran {
    requires javafx.controls;
    requires javafx.swing;
    requires transitive javafx.graphics;
    requires transitive java.desktop;
    requires transitive java.sql;
    requires org.apache.pdfbox;
    requires transitive jfx.incubator.richtext;
    requires com.dlsc.pdfviewfx;
    exports com.adham_omran;
}
