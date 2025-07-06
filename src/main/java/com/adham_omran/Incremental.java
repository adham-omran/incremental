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
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.adham_omran.Database;

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

        // PDF Loading
        File pdfFile = new File("/Users/adham/code/incremental-minimal/src/main/java/com/adham_omran/test.pdf");

        PDDocument doc = Loader.loadPDF(pdfFile);

        PDFRenderer rndr = new PDFRenderer(doc);

        // Render at high DPI for quality, then scale down for display
        BufferedImage highQualityImage = rndr.renderImageWithDPI(0, 300);
        System.out.println("High quality image class: " + highQualityImage.getClass());
        System.out.println("Converted FX image class: " + bufferedImageToFXImage(highQualityImage).getClass());

        // Image Viewing with scaling
        ImageView iv2 = new ImageView(bufferedImageToFXImage(highQualityImage));

        // Scale down for display (300 DPI -> 72 DPI equivalent)
        double scaleFactor = 72.0 / 300.0; // Scale down by ~24%
        iv2.setFitWidth(highQualityImage.getWidth() * scaleFactor);
        iv2.setFitHeight(highQualityImage.getHeight() * scaleFactor);
        iv2.setPreserveRatio(true);
        iv2.setSmooth(true); // Enable smooth scaling

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
            try (FileInputStream fis = new FileInputStream(img)) {
                Database.main(fis, (int) img.length());
                System.out.println("Image added.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        gp.setPadding(new Insets(10));
        gp.setHgap(4);
        gp.setVgap(8);

        gp.add(xLabel, 0, 1);
        gp.add(xField, 1, 1);
        gp.add(btn, 0, 3);
        gp.add(btnDatabase, 0, 4);
        // gp.add(iv2, 0, 4);

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
