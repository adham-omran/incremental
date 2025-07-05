package com.adham_omran;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;

/**
 * JavaFX App
 */
public class Incremental extends Application {

    private WritableImage captureScreenshot(int x, int y, int width, int height) {
        Robot robot = new Robot();
        try {
            return robot.getScreenCapture(null, x, y, width, height);
        }
        catch (IllegalArgumentException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public void start(Stage stage) {
        // Stage <- Scene <- Pane
        VBox vbox = new VBox();

        GridPane gp = new GridPane();

        Label xLabel = new Label("X");
        Label yLabel = new Label("Y");
        Label widthLabel = new Label("Width");
        Label heightLabel = new Label("Height");

        TextField xField = new TextField();

        gp.setPadding(new Insets(10));
        gp.setHgap( 4 );
        gp.setVgap( 8 );

        gp.add(xLabel, 0, 1);
        gp.add(xField, 1, 1);

        // var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(new Scene(gp, 640, 480));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
