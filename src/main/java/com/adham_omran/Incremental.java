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
import javafx.stage.FileChooser;

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
    private HBox currentButtonBox;

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
        Button btnAddPDF = new Button("Add PDF");
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

            // Check if clipboard contains an image
            if (awtImage == null) {
                btnClipboard.setText("No image in clipboard");
                btnClipboard.setDisable(false);
                // Reset button text after 3 seconds
                Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(3), resetEvent -> {
                    btnClipboard.setText(originalString);
                }));
                resetTimer.play();
                return;
            }

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
                // Reset button text after 3 seconds
                Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(3), resetEvent -> {
                    btnClipboard.setText(originalString);
                }));
                resetTimer.play();

                e.printStackTrace();
            }
        });

        btnAddPDF.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select PDF File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                Database dbDatabase = new Database();
                String originalText = btnAddPDF.getText();
                btnAddPDF.setText("Loading...");
                btnAddPDF.setDisable(true);

                try {
                    dbDatabase.savePDF(selectedFile.getAbsolutePath());
                    btnAddPDF.setText("PDF Loaded!");

                    // Reset button after 2 seconds
                    Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(2), resetEvent -> {
                        btnAddPDF.setText(originalText);
                        btnAddPDF.setDisable(false);
                    }));
                    resetTimer.play();

                } catch (Exception e) {
                    btnAddPDF.setText("Load Failed");
                    btnAddPDF.setDisable(false);

                    Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(2), resetEvent -> {
                        btnAddPDF.setText(originalText);
                    }));
                    resetTimer.play();

                    e.printStackTrace();
                }
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

            Stage itemStage = new Stage();
            itemStage.setTitle("Item");

            currentImageView = new ImageView();

            Image img = null;
            if (currentTopic.isPdf()) {
                // Render PDF page
                try {
                    PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                    img = PDFImageRenderer.renderPageToFXImage(pdfInfo, currentTopic.getCurrentPage());
                    System.out.println("Rendered PDF page " + currentTopic.getCurrentPage());
                } catch (Exception ex) {
                    System.err.println("Error rendering PDF: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                img = currentTopic.getTopicImage();
            }

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
            currentScrollPane.setContextMenu(contextMenu);

            Button btnNextItem = new Button("Next Item");
            btnNextItem.setOnAction(this::handleNextItem);

            Button btnClose = new Button("Close");
            btnClose.setOnAction(this::handleClose);

            currentButtonBox = new HBox();
            currentButtonBox.getChildren().addAll(btnNextItem, btnClose);

            // Add PDF controls if this is a PDF topic
            if (currentTopic.isPdf()) {
                Button btnPrevPage = new Button("Previous Page");
                Button btnNextPage = new Button("Next Page");
                Button btnFitToPage = new Button("Fit to Page");
                Button btnFitToWidth = new Button("Fit to Width");

                btnPrevPage.setOnAction(event -> {
                    if (currentTopic.getCurrentPage() > 1) {
                        int newPage = currentTopic.getCurrentPage() - 1;
                        currentTopic.setCurrentPage(newPage);
                        database.updatePDFPage(currentTopic.getRowId(), newPage);

                        // Re-render the page
                        try {
                            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                            Image newImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, newPage);
                            currentImageView.setImage(newImg);
                            System.out.println("Moved to page " + newPage);
                        } catch (Exception ex) {
                            System.err.println("Error rendering PDF page: " + ex.getMessage());
                        }
                    }
                });

                btnNextPage.setOnAction(event -> {
                    try {
                        PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                        if (currentTopic.getCurrentPage() < pdfInfo.getTotalPages()) {
                            int newPage = currentTopic.getCurrentPage() + 1;
                            currentTopic.setCurrentPage(newPage);
                            database.updatePDFPage(currentTopic.getRowId(), newPage);

                            // Re-render the page
                            Image newImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, newPage);
                            currentImageView.setImage(newImg);
                            System.out.println("Moved to page " + newPage);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error rendering PDF page: " + ex.getMessage());
                    }
                });

                btnFitToPage.setOnAction(event -> {
                    // Fit image to fill the entire scroll pane
                    currentImageView.setPreserveRatio(true);
                    currentImageView.setFitWidth(currentScrollPane.getWidth() - 20); // Account for padding
                    currentImageView.setFitHeight(currentScrollPane.getHeight() - 20); // Account for padding
                    currentScrollPane.setFitToWidth(false);
                    currentScrollPane.setFitToHeight(false);
                    System.out.println("Fit to page");
                });

                btnFitToWidth.setOnAction(event -> {
                    // Fit image width to the scroll pane width
                    currentImageView.setPreserveRatio(true);
                    currentImageView.setFitWidth(currentScrollPane.getWidth() - 20); // Account for padding
                    currentImageView.setFitHeight(0); // Let height adjust automatically
                    currentScrollPane.setFitToWidth(false);
                    currentScrollPane.setFitToHeight(false);
                    System.out.println("Fit to width");
                });

                currentButtonBox.getChildren().addAll(btnPrevPage, btnNextPage, btnFitToPage, btnFitToWidth);
            }

            currentButtonBox.setSpacing(10);
            currentButtonBox.setPadding(new Insets(10));

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
                    currentButtonBox,
                    currentScrollPane,
                    currentRichTextArea);
            vboxItem.setSpacing(10);
            vboxItem.setPadding(new Insets(10));

            Scene itemScene = new Scene(vboxItem, 700, 600);
            itemStage.setScene(itemScene);

            // Bind currentScrollPane max height to 80% of stage height
            currentScrollPane.maxHeightProperty().bind(itemStage.heightProperty().multiply(0.8));

            itemStage.show();

            System.out.println("Reading image.");
        });

        btnTopicWithId.setOnAction(e -> {
            currentTopic = database.findTopic(Integer.valueOf(txtInput.getText()));
            if (currentTopic == null) {
                System.out.println("No topics available.");
                return;
            }

            Stage itemStage = new Stage();
            itemStage.setTitle("Item");

            currentImageView = new ImageView();

            Image img = null;
            if (currentTopic.isPdf()) {
                // Render PDF page
                try {
                    PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                    img = PDFImageRenderer.renderPageToFXImage(pdfInfo, currentTopic.getCurrentPage());
                    System.out.println("Rendered PDF page " + currentTopic.getCurrentPage());
                } catch (Exception ex) {
                    System.err.println("Error rendering PDF: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                img = currentTopic.getTopicImage();
            }

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
            currentScrollPane.setContextMenu(contextMenu);

            Button btnNextItem = new Button("Next Item");
            btnNextItem.setOnAction(this::handleNextItem);

            Button btnClose = new Button("Close");
            btnClose.setOnAction(this::handleClose);

            currentButtonBox = new HBox();
            currentButtonBox.getChildren().addAll(btnNextItem, btnClose);

            // Add PDF controls if this is a PDF topic
            if (currentTopic.isPdf()) {
                Button btnPrevPage = new Button("Previous Page");
                Button btnNextPage = new Button("Next Page");
                Button btnFitToPage = new Button("Fit to Page");
                Button btnFitToWidth = new Button("Fit to Width");

                btnPrevPage.setOnAction(event -> {
                    if (currentTopic.getCurrentPage() > 1) {
                        int newPage = currentTopic.getCurrentPage() - 1;
                        currentTopic.setCurrentPage(newPage);
                        database.updatePDFPage(currentTopic.getRowId(), newPage);

                        // Re-render the page
                        try {
                            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                            Image newImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, newPage);
                            currentImageView.setImage(newImg);
                            System.out.println("Moved to page " + newPage);
                        } catch (Exception ex) {
                            System.err.println("Error rendering PDF page: " + ex.getMessage());
                        }
                    }
                });

                btnNextPage.setOnAction(event -> {
                    try {
                        PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                        if (currentTopic.getCurrentPage() < pdfInfo.getTotalPages()) {
                            int newPage = currentTopic.getCurrentPage() + 1;
                            currentTopic.setCurrentPage(newPage);
                            database.updatePDFPage(currentTopic.getRowId(), newPage);

                            // Re-render the page
                            Image newImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, newPage);
                            currentImageView.setImage(newImg);
                            System.out.println("Moved to page " + newPage);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error rendering PDF page: " + ex.getMessage());
                    }
                });

                btnFitToPage.setOnAction(event -> {
                    // Fit image to fill the entire scroll pane
                    currentImageView.setPreserveRatio(true);
                    currentImageView.setFitWidth(currentScrollPane.getWidth() - 20); // Account for padding
                    currentImageView.setFitHeight(currentScrollPane.getHeight() - 20); // Account for padding
                    currentScrollPane.setFitToWidth(false);
                    currentScrollPane.setFitToHeight(false);
                    System.out.println("Fit to page");
                });

                btnFitToWidth.setOnAction(event -> {
                    // Fit image width to the scroll pane width
                    currentImageView.setPreserveRatio(true);
                    currentImageView.setFitWidth(currentScrollPane.getWidth() - 20); // Account for padding
                    currentImageView.setFitHeight(0); // Let height adjust automatically
                    currentScrollPane.setFitToWidth(false);
                    currentScrollPane.setFitToHeight(false);
                    System.out.println("Fit to width");
                });

                currentButtonBox.getChildren().addAll(btnPrevPage, btnNextPage, btnFitToPage, btnFitToWidth);
            }

            currentButtonBox.setSpacing(10);
            currentButtonBox.setPadding(new Insets(10));

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
                    currentButtonBox,
                    currentScrollPane,
                    currentRichTextArea);
            vboxItem.setSpacing(10);
            vboxItem.setPadding(new Insets(10));

            Scene itemScene = new Scene(vboxItem, 700, 600);
            itemStage.setScene(itemScene);

            // Bind currentScrollPane max height to 80% of stage height
            currentScrollPane.maxHeightProperty().bind(itemStage.heightProperty().multiply(0.8));

            itemStage.show();

            System.out.println("Reading image.");
        });

        gp.setPadding(new Insets(10));
        gp.setHgap(4);
        gp.setVgap(8);

        gp.add(btnClipboard, 0, 1);
        gp.add(btnAddPDF, 1, 1);
        gp.add(btnTable, 2, 1);
        gp.add(btnNext, 3, 1);
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
            Image nextImg = null;

            if (currentTopic.isPdf()) {
                // Render PDF page
                try {
                    PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                    nextImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, currentTopic.getCurrentPage());
                    System.out.println("Rendered PDF page " + currentTopic.getCurrentPage());
                } catch (Exception ex) {
                    System.err.println("Error rendering PDF: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                nextImg = currentTopic.getTopicImage();
            }

            if (nextImg != null) {
                // Topic has an image or PDF - show it
                currentImageView.setImage(nextImg);
                currentScrollPane.setVisible(true);
                currentScrollPane.setManaged(true);
                System.out.println("Next image/PDF and content loaded for rowId: " + currentTopic.getRowId());
            } else {
                // Topic has no image - hide the scroll pane
                currentScrollPane.setVisible(false);
                currentScrollPane.setManaged(false);
                System.out.println("Next content loaded (no image) for rowId: " + currentTopic.getRowId());
            }
            // Always load the topic's content into the RichTextArea
            database.loadContentIntoRichTextArea(currentTopic.getContent(), currentRichTextArea);
            
            // Update PDF controls based on the new topic
            updatePDFControls();
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
    
    private void updatePDFControls() {
        if (currentButtonBox == null) return;
        
        // Remove any existing PDF controls (everything after the first 2 buttons: Next Item and Close)
        if (currentButtonBox.getChildren().size() > 2) {
            currentButtonBox.getChildren().subList(2, currentButtonBox.getChildren().size()).clear();
        }
        
        // Add PDF controls if this is a PDF topic
        if (currentTopic != null && currentTopic.isPdf()) {
            Button btnPrevPage = new Button("Previous Page");
            Button btnNextPage = new Button("Next Page");
            Button btnFitToPage = new Button("Fit to Page");
            Button btnFitToWidth = new Button("Fit to Width");
            
            btnPrevPage.setOnAction(event -> {
                if (currentTopic.getCurrentPage() > 1) {
                    int newPage = currentTopic.getCurrentPage() - 1;
                    currentTopic.setCurrentPage(newPage);
                    database.updatePDFPage(currentTopic.getRowId(), newPage);
                    
                    // Re-render the page
                    try {
                        PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                        Image newImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, newPage);
                        currentImageView.setImage(newImg);
                        System.out.println("Moved to page " + newPage);
                    } catch (Exception ex) {
                        System.err.println("Error rendering PDF page: " + ex.getMessage());
                    }
                }
            });
            
            btnNextPage.setOnAction(event -> {
                try {
                    PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                    if (currentTopic.getCurrentPage() < pdfInfo.getTotalPages()) {
                        int newPage = currentTopic.getCurrentPage() + 1;
                        currentTopic.setCurrentPage(newPage);
                        database.updatePDFPage(currentTopic.getRowId(), newPage);
                        
                        // Re-render the page
                        Image newImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, newPage);
                        currentImageView.setImage(newImg);
                        System.out.println("Moved to page " + newPage);
                    }
                } catch (Exception ex) {
                    System.err.println("Error rendering PDF page: " + ex.getMessage());
                }
            });
            
            btnFitToPage.setOnAction(event -> {
                // Fit image to fill the entire scroll pane
                currentImageView.setPreserveRatio(true);
                currentImageView.setFitWidth(currentScrollPane.getWidth() - 20); // Account for padding
                currentImageView.setFitHeight(currentScrollPane.getHeight() - 20); // Account for padding
                currentScrollPane.setFitToWidth(false);
                currentScrollPane.setFitToHeight(false);
                System.out.println("Fit to page");
            });
            
            btnFitToWidth.setOnAction(event -> {
                // Fit image width to the scroll pane width
                currentImageView.setPreserveRatio(true);
                currentImageView.setFitWidth(currentScrollPane.getWidth() - 20); // Account for padding
                currentImageView.setFitHeight(0); // Let height adjust automatically
                currentScrollPane.setFitToWidth(false);
                currentScrollPane.setFitToHeight(false);
                System.out.println("Fit to width");
            });
            
            currentButtonBox.getChildren().addAll(btnPrevPage, btnNextPage, btnFitToPage, btnFitToWidth);
        }
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
