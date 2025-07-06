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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.embed.swing.SwingFXUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.adham_omran.Database;
import com.adham_omran.ClipboardUtils;

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
    public void start(Stage stage) throws IOException {
        // Stage <- Scene <- Pane
        VBox vbox = new VBox();

        GridPane gp = new GridPane();

        Label xLabel = new Label("X");
        Label yLabel = new Label("Y");
        Label widthLabel = new Label("Width");
        Label heightLabel = new Label("Height");

        TextField xField = new TextField();

        Button btn = new Button("Open PDF");
        Button btnDatabase = new Button("Do stuff with db");
        Button btnReadImage = new Button("Read Image");
        Button btnClipboard = new Button("Get from Clipboard");

        btn.setOnAction(event -> {
            Stage imageStage = new Stage();
            imageStage.setTitle("View");

            ImageView iv1 = new ImageView(new Image(getClass().getResourceAsStream("/image.jpg")));
            iv1.setFitWidth(500);
            iv1.setPreserveRatio(true);

            Scene imageScene = new Scene(new StackPane(iv1), 500, 500);
            imageStage.setScene(imageScene);
            imageStage.show();
        });


        btnDatabase.setOnAction(event -> {
            // Put the image in the database
                File img = new File("/Users/adham/code/incremental-minimal/src/main/resources/image.jpg");
            Database dbDatabase = new Database();
            try (FileInputStream fis = new FileInputStream(img)) {
                dbDatabase.saveImage(fis, (int) img.length());
                System.out.println("Image added.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        btnReadImage.setOnAction(event -> {
            Database dbDatabase = new Database();
            Image img = dbDatabase.readImage();
            Stage imageStage = new Stage();
            imageStage.setTitle("View");

            ImageView iv1 = new ImageView();
            iv1.setImage(img);
            iv1.setFitWidth(500);
            iv1.setPreserveRatio(true);

            Scene imageScene = new Scene(new StackPane(iv1), 500, 500);
            imageStage.setScene(imageScene);
            imageStage.show();

            System.out.println("Reading image.");
        });

        btnClipboard.setOnAction(event -> {
                // Save to DB
            Database dbDatabase = new Database();
            ClipboardUtils cp = new ClipboardUtils();

            BufferedImage bufferedImage;
            java.awt.Image awtImage = cp.getImageFromClipboard();

            // Convert to BufferedImage if it isn't already
            if (awtImage instanceof BufferedImage) {
                bufferedImage = (BufferedImage) awtImage;
            } else {
                bufferedImage = new BufferedImage(awtImage.getWidth(null),
                                                  awtImage.getHeight(null),
                                                  BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(awtImage, 0, 0, null);
                g2d.dispose();
            }
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", os);
                InputStream fis = new ByteArrayInputStream(os.toByteArray());
                dbDatabase.saveImage(fis, os.size());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        gp.setPadding(new Insets(10));
        gp.setHgap(4);
        gp.setVgap(8);

        gp.add(xLabel, 0, 1);
        gp.add(xField, 1, 1);
        gp.add(btn, 0, 3);
        gp.add(btnDatabase, 0, 4);
        gp.add(btnReadImage, 0, 5);
        gp.add(btnClipboard, 0, 6);

        // var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(new Scene(gp, 640, 480));
        stage.show();
    }

    public static Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        try {
            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // Create JavaFX Image from byte array
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            return new Image(bais);

        } catch (IOException e) {
            throw new RuntimeException("Failed to convert BufferedImage to JavaFX Image", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
