package com.adham_omran;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * JavaFX App
 */
public class Incremental extends Application {

    private ImageView currentImageView;
    private ScrollPane currentScrollPane;
    private Timeline saveTimer;
    private Database database;
    private RichTextArea currentRichTextArea;
    private Topic currentTopic;

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
        // Initialize database
        database = new Database();

        // Stage <- Scene <- Pane
        GridPane gp = new GridPane();

        Button btnClipboard = new Button("Save from Clipboard");
        Button btnTable = new Button("View Table");
        Button btnNext = new Button("Next Item");

        TextField txtInput = new TextField("Enter ID");
        Button btnTopicWithId = new Button("Open Topic with ID: ");

        RichTextArea rta = new RichTextArea();

        btnClipboard.setOnAction(event -> {
            // Save to DB
            Database dbDatabase = new Database();
            ClipboardUtils cp = new ClipboardUtils();
            String originalString = btnClipboard.getText();
            btnClipboard.setText("Saving...");
            btnClipboard.setDisable(true);

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
                btnClipboard.setText(originalString);
                btnClipboard.setDisable(false);

            } catch (IOException e) {
                btnClipboard.setText("Save failed.");
                btnClipboard.setDisable(false);

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
            currentTopic = database.nextTopic();
            if (currentTopic == null) {
                System.out.println("No topics available.");
                return;
            }

            Image img = currentTopic.getTopicImage();
            Stage itemStage = new Stage();
            itemStage.setTitle("Item");

            currentImageView = new ImageView();
            currentImageView.setImage(img);

            currentImageView.setFitWidth(600);
            currentImageView.setPreserveRatio(true);

            currentScrollPane = new ScrollPane();
            currentScrollPane.setContent(currentImageView);
            currentScrollPane.setFitToWidth(true);
            currentScrollPane.setFitToHeight(true);
            
            // Hide scroll pane if no image
            if (img == null) {
                currentScrollPane.setVisible(false);
                currentScrollPane.setManaged(false);
            }

            // Create context menu for image copying
            ContextMenu contextMenu = new ContextMenu();
            MenuItem copyItem = new MenuItem("Copy Image");
            copyItem.setOnAction(event -> {
                Image currentImage = currentImageView.getImage();
                if (currentImage != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putImage(currentImage);
                    clipboard.setContent(content);
                    System.out.println("Image copied to clipboard.");
                }
            });
            contextMenu.getItems().add(copyItem);
            scrollPane.setContextMenu(contextMenu);

            Button btnNextItem = new Button("Next Item");
            btnNextItem.setOnAction(this::handleNextItem);

            Button btnClose = new Button("Close");
            btnClose.setOnAction(this::handleClose);

            HBox hboxItem = new HBox();
            hboxItem.getChildren().addAll(btnNextItem, btnClose);
            hboxItem.setSpacing(10);
            hboxItem.setPadding(new Insets(10));

            currentRichTextArea = new RichTextArea();
            // Load existing content using the database method
            database.loadContentIntoRichTextArea(currentTopic.getContent(), currentRichTextArea);

            // Approach 1: Model property listener for change detection with debouncing
            currentRichTextArea.modelProperty().addListener((obs, oldModel, newModel) -> {
                if (newModel != null) {
                    // For now, we'll rely on focus-based saving as the primary mechanism
                    // since the exact StyledTextModel change listener API needs further research
                    System.out.println("Model changed - content change detection active");
                }
            });

            // Approach 2: Focus-based saving (primary mechanism)
            currentRichTextArea.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (wasFocused && !isFocused) {
                    // Save immediately when focus is lost
                    if (saveTimer != null) {
                        saveTimer.stop();
                    }
                    database.updateContent(currentTopic.getRowId(), currentRichTextArea);
                }
            });

            // Additional: Keyboard-based debounced saving as backup
            currentRichTextArea.setOnKeyReleased(event -> {
                // Restart the save timer on each keystroke
                if (saveTimer != null) {
                    saveTimer.stop();
                }
                saveTimer = new Timeline(new KeyFrame(Duration.seconds(2), saveEvent -> {
                    database.updateContent(currentTopic.getRowId(), currentRichTextArea);
                }));
                saveTimer.play();
            });

            VBox vboxItem = new VBox();
            vboxItem.getChildren().addAll(
                    hboxItem,
                    scrollPane,
                    currentRichTextArea);
            vboxItem.setSpacing(10);
            vboxItem.setPadding(new Insets(10));

            Scene itemScene = new Scene(vboxItem, 700, 600);
            itemStage.setScene(itemScene);

            // Bind scrollPane max height to 80% of stage height
            scrollPane.maxHeightProperty().bind(itemStage.heightProperty().multiply(0.8));

            itemStage.show();

            System.out.println("Reading image.");
        });

        btnTopicWithId.setOnAction(e -> {
            currentTopic = database.findTopic(Integer.valueOf(txtInput.getText()));
            if (currentTopic == null) {
                System.out.println("No topics available.");
                return;
            }

            Image img = currentTopic.getTopicImage();
            Stage itemStage = new Stage();
            itemStage.setTitle("Item");

            currentImageView = new ImageView();
            currentImageView.setImage(img);

            currentImageView.setFitWidth(600);
            currentImageView.setPreserveRatio(true);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(currentImageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            // Create context menu for image copying
            ContextMenu contextMenu = new ContextMenu();
            MenuItem copyItem = new MenuItem("Copy Image");
            copyItem.setOnAction(event -> {
                Image currentImage = currentImageView.getImage();
                if (currentImage != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putImage(currentImage);
                    clipboard.setContent(content);
                    System.out.println("Image copied to clipboard.");
                }
            });
            contextMenu.getItems().add(copyItem);
            scrollPane.setContextMenu(contextMenu);

            Button btnNextItem = new Button("Next Item");
            btnNextItem.setOnAction(this::handleNextItem);

            Button btnClose = new Button("Close");
            btnClose.setOnAction(this::handleClose);

            HBox hboxItem = new HBox();
            hboxItem.getChildren().addAll(btnNextItem, btnClose);
            hboxItem.setSpacing(10);
            hboxItem.setPadding(new Insets(10));

            currentRichTextArea = new RichTextArea();
            // Load existing content using the database method
            database.loadContentIntoRichTextArea(currentTopic.getContent(), currentRichTextArea);

            // Approach 1: Model property listener for change detection with debouncing
            currentRichTextArea.modelProperty().addListener((obs, oldModel, newModel) -> {
                if (newModel != null) {
                    // For now, we'll rely on focus-based saving as the primary mechanism
                    // since the exact StyledTextModel change listener API needs further research
                    System.out.println("Model changed - content change detection active");
                }
            });

            // Approach 2: Focus-based saving (primary mechanism)
            currentRichTextArea.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (wasFocused && !isFocused) {
                    // Save immediately when focus is lost
                    if (saveTimer != null) {
                        saveTimer.stop();
                    }
                    database.updateContent(currentTopic.getRowId(), currentRichTextArea);
                }
            });

            // Additional: Keyboard-based debounced saving as backup
            currentRichTextArea.setOnKeyReleased(event -> {
                // Restart the save timer on each keystroke
                if (saveTimer != null) {
                    saveTimer.stop();
                }
                saveTimer = new Timeline(new KeyFrame(Duration.seconds(2), saveEvent -> {
                    database.updateContent(currentTopic.getRowId(), currentRichTextArea);
                }));
                saveTimer.play();
            });

            VBox vboxItem = new VBox();
            vboxItem.getChildren().addAll(
                    hboxItem,
                    scrollPane,
                    currentRichTextArea);
            vboxItem.setSpacing(10);
            vboxItem.setPadding(new Insets(10));

            Scene itemScene = new Scene(vboxItem, 700, 600);
            itemStage.setScene(itemScene);

            // Bind scrollPane max height to 80% of stage height
            scrollPane.maxHeightProperty().bind(itemStage.heightProperty().multiply(0.8));

            itemStage.show();

            System.out.println("Reading image.");
        });

        gp.setPadding(new Insets(10));
        gp.setHgap(4);
        gp.setVgap(8);

        gp.add(btnClipboard, 0, 1);
        gp.add(btnTable, 1, 1);
        gp.add(btnNext, 2, 1);
        gp.add(btnTopicWithId, 0, 2);
        gp.add(txtInput, 1, 2);

        // var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(new Scene(gp, 640, 480));
        stage.show();
    }

    private void handleNextItem(ActionEvent e) {
        // Save current content before switching if there's unsaved work
        if (saveTimer != null) {
            saveTimer.stop();
        }
        if (currentTopic != null && currentRichTextArea != null) {
            database.updateContent(currentTopic.getRowId(), currentRichTextArea);
        }

        // Load next topic
        currentTopic = database.nextTopic();
        if (currentTopic != null) {
            Image nextImg = currentTopic.getTopicImage();
            if (currentImageView != null && nextImg != null) {
                currentImageView.setImage(nextImg);
                // Load the new topic's content into the RichTextArea
                database.loadContentIntoRichTextArea(currentTopic.getContent(), currentRichTextArea);
                System.out.println("Next image and content loaded for rowId: " + currentTopic.getRowId());
            } else {
                System.out.println("Image view or image is null.");
            }
        } else {
            System.out.println("No more topics available.");
        }
    }

    private void handleClose(ActionEvent e) {
        // Save current content before closing
        if (saveTimer != null) {
            saveTimer.stop();
        }
        if (currentTopic != null && currentRichTextArea != null) {
            database.updateContent(currentTopic.getRowId(), currentRichTextArea);
        }

        // Close the window
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
