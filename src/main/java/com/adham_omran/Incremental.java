package com.adham_omran;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import java.awt.MouseInfo;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * JavaFX App
 */
public class Incremental extends Application {

    private boolean drawMode = false;
    private double startX, startY;
    private Rectangle selectionRect;
    private Pane drawingPane;

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

        Button btn = new Button("Open PDF");

        btn.setOnAction(event -> {
                System.out.println(MouseInfo.getPointerInfo().getLocation());
            });

        gp.setPadding(new Insets(10));
        gp.setHgap( 4 );
        gp.setVgap( 8 );

        gp.add(xLabel, 0, 1);
        gp.add(xField, 1, 1);
        gp.add(btn, 0, 3);

        // var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(new Scene(gp, 640, 480));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
