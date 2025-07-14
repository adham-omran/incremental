package com.adham_omran;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;

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
    private ImageView currentImageView;

    private WritableImage captureScreenshot(int x, int y, int width, int height) {
        Robot robot = new Robot();
        try {
            return robot.getScreenCapture(null, x, y, width, height);
        } catch (IllegalArgumentException exception) {
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
        Button btnClipboard = new Button("Save from Clipboard");
        Button btnTable = new Button("View Table");
        Button btnNext = new Button("Next Item");

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
            iv1.setFitWidth(img.getWidth());
            iv1.setPreserveRatio(true);

            Scene imageScene = new Scene(new StackPane(iv1));
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
                System.out.println("Conversion Success.");
                bufferedImage = (BufferedImage) awtImage;
            } else {
                System.out.println("Conversion in progress...");
                bufferedImage = new BufferedImage(awtImage.getWidth(null),
                        awtImage.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(awtImage, 0, 0, null);
                g2d.dispose();
                System.out.println("Conversion done.");
            }
            try {
                // Save the image
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", os);
                InputStream fis = new ByteArrayInputStream(os.toByteArray());
                dbDatabase.saveImage(fis, os.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnTable.setOnAction(e -> {
            System.out.println("Clicked btnTable.");
            Stage stageTable = new Stage();
            Group root = new Group();
            Scene scene = new Scene(root);
            stageTable.setTitle("Table View Sample");

            TableView<String> table = new TableView<>();
            table.setEditable(false);
            TableColumn<String, String> firstNameCol = new TableColumn<>("First Name");
            TableColumn<String, String> lastNameCol = new TableColumn<>("Last Name");

            table.getColumns().setAll(firstNameCol, lastNameCol);

            VBox vboxTable = new VBox();
            vboxTable.setSpacing(5);
            vboxTable.setPadding(new Insets(10, 0, 0, 10));
            vboxTable.getChildren().addAll(table);
            ((Group) scene.getRoot()).getChildren().add(vboxTable);
            stageTable.setScene(scene);
            stageTable.show();

        });


        btnNext.setOnAction(e -> {
            Database dbDatabase = new Database();
            Image img = dbDatabase.nextImage();
            Stage imageStage = new Stage();
            imageStage.setTitle("Item");

            currentImageView = new ImageView();
            currentImageView.setImage(img);


            currentImageView.setFitWidth(600);
            currentImageView.setPreserveRatio(true);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(currentImageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefSize(650, 500);

            Button btnNextItem = new Button("Next Item");
            btnNextItem.setOnAction(this::handleNextItem);

            Button btnClose = new Button("Close");
            btnClose.setOnAction(this::handleClose);

            VBox vboxItem = new VBox();
            vboxItem.getChildren().addAll(scrollPane,
                                          new Button("FooBar"),
                                          btnNextItem,
                                          btnClose);
            vboxItem.setSpacing(10);
            vboxItem.setPadding(new Insets(10));

            Scene itemScene = new Scene(vboxItem, 700, 600);
            imageStage.setScene(itemScene);
            imageStage.show();

            System.out.println("Reading image.");
        });

        gp.setPadding(new Insets(10));
        gp.setHgap(4);
        gp.setVgap(8);

        gp.add(btn, 0, 3);
        gp.add(btnDatabase, 0, 4);
        gp.add(btnReadImage, 0, 5);
        gp.add(btnClipboard, 0, 6);
        gp.add(btnTable, 0, 7);
        gp.add(btnNext, 0, 8);

        // var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(new Scene(gp, 640, 480));
        stage.show();
    }

    private void handleNextItem(ActionEvent e) {
        Database dbDatabase = new Database();
        Image nextImg = dbDatabase.nextImage();
        if (currentImageView != null && nextImg != null) {
            currentImageView.setImage(nextImg);
            System.out.println("Next image loaded.");
        } else {
            System.out.println("Image view or image is null.");
        }
    }

    private void handleClose(ActionEvent e) {
        // Close the window.
        Button source = (Button) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
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
