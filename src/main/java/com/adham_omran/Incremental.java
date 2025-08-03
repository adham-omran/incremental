package com.adham_omran;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import jfx.incubator.scene.control.richtext.RichTextArea;
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
    private TextField pageNumberField;
    private Label totalPagesLabel;

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

        // Create main container with improved layout
        VBox mainContainer = new VBox();
        mainContainer.getStyleClass().add("main-container");

        // Content Actions Section
        VBox contentSection = new VBox();
        contentSection.getStyleClass().add("section-container");
        
        Label contentLabel = new Label("Content Actions");
        contentLabel.getStyleClass().add("section-title");
        
        HBox contentButtons = new HBox();
        contentButtons.getStyleClass().add("button-group");
        
        Button btnClipboard = new Button("Save from Clipboard");
        btnClipboard.getStyleClass().add("primary-button");
        btnClipboard.setTooltip(new Tooltip("Save an image from your clipboard to the database"));
        
        Button btnAddPDF = new Button("Add PDF");
        btnAddPDF.getStyleClass().add("secondary-button");
        btnAddPDF.setTooltip(new Tooltip("Import a PDF file for study"));
        
        contentButtons.getChildren().addAll(btnClipboard, btnAddPDF);
        contentSection.getChildren().addAll(contentLabel, contentButtons);

        // Navigation Section
        VBox navigationSection = new VBox();
        navigationSection.getStyleClass().add("section-container");
        
        Label navigationLabel = new Label("Navigation");
        navigationLabel.getStyleClass().add("section-title");
        
        HBox navigationButtons = new HBox();
        navigationButtons.getStyleClass().add("button-group");
        
        Button btnNext = new Button("Next Topic");
        btnNext.getStyleClass().add("secondary-button");
        btnNext.setTooltip(new Tooltip("Open the next topic scheduled for review"));
        
        Button btnTable = new Button("View Table");
        btnTable.getStyleClass().add("tertiary-button");
        btnTable.setTooltip(new Tooltip("View all topics in a sortable table"));
        
        navigationButtons.getChildren().addAll(btnNext, btnTable);
        navigationSection.getChildren().addAll(navigationLabel, navigationButtons);

        // Direct Access Section
        VBox accessSection = new VBox();
        accessSection.getStyleClass().add("section-container");
        
        Label accessLabel = new Label("Direct Access");
        accessLabel.getStyleClass().add("section-title");
        
        HBox accessForm = new HBox();
        accessForm.getStyleClass().add("form-group");
        
        Label idLabel = new Label("Topic ID:");
        TextField txtInput = new TextField();
        txtInput.setPromptText("Enter topic ID...");
        txtInput.getStyleClass().add("text-field");
        txtInput.setTooltip(new Tooltip("Enter a specific topic ID to open directly"));
        
        Button btnTopicWithId = new Button("🔍 Open");
        btnTopicWithId.getStyleClass().add("tertiary-button");
        btnTopicWithId.setTooltip(new Tooltip("Open the topic with the specified ID"));
        
        // Add Enter key support for the ID field
        txtInput.setOnAction(event -> btnTopicWithId.fire());
        
        accessForm.getChildren().addAll(idLabel, txtInput, btnTopicWithId);
        accessSection.getChildren().addAll(accessLabel, accessForm);

        // Add all sections to main container
        mainContainer.getChildren().addAll(contentSection, navigationSection, accessSection);

        btnClipboard.setOnAction(event -> {
            // Save to DB
            Database dbDatabase = new Database();
            ClipboardUtils cp = new ClipboardUtils();
            String originalString = btnClipboard.getText();
            btnClipboard.setText("💾 Saving...");
            btnClipboard.getStyleClass().removeAll("primary-button");
            btnClipboard.getStyleClass().add("loading-button");
            btnClipboard.setDisable(true);

            BufferedImage bufferedImage;
            java.awt.Image awtImage = cp.getImageFromClipboard();

            // Check if clipboard contains an image
            if (awtImage == null) {
                btnClipboard.setText("❌ No image in clipboard");
                btnClipboard.getStyleClass().removeAll("loading-button");
                btnClipboard.getStyleClass().add("error-button");
                btnClipboard.setDisable(false);
                // Reset button text after 3 seconds
                Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(3), resetEvent -> {
                    btnClipboard.setText(originalString);
                    btnClipboard.getStyleClass().removeAll("error-button");
                    btnClipboard.getStyleClass().add("primary-button");
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
                btnClipboard.setText("✅ Saved!");
                btnClipboard.getStyleClass().removeAll("loading-button");
                btnClipboard.getStyleClass().add("success-button");
                btnClipboard.setDisable(false);
                
                // Reset button after 2 seconds
                Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(2), resetEvent -> {
                    btnClipboard.setText(originalString);
                    btnClipboard.getStyleClass().removeAll("success-button");
                    btnClipboard.getStyleClass().add("primary-button");
                }));
                resetTimer.play();

            } catch (IOException e) {
                btnClipboard.setText("❌ Save failed");
                btnClipboard.getStyleClass().removeAll("loading-button");
                btnClipboard.getStyleClass().add("error-button");
                btnClipboard.setDisable(false);
                // Reset button text after 3 seconds
                Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(3), resetEvent -> {
                    btnClipboard.setText(originalString);
                    btnClipboard.getStyleClass().removeAll("error-button");
                    btnClipboard.getStyleClass().add("primary-button");
                }));
                resetTimer.play();

                e.printStackTrace();
            }
        });

        btnAddPDF.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select PDF File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                Database dbDatabase = new Database();
                String originalText = btnAddPDF.getText();
                btnAddPDF.setText("📥 Loading...");
                btnAddPDF.getStyleClass().removeAll("secondary-button");
                btnAddPDF.getStyleClass().add("loading-button");
                btnAddPDF.setDisable(true);

                try {
                    dbDatabase.savePDF(selectedFile.getAbsolutePath());
                    btnAddPDF.setText("✅ PDF Loaded!");
                    btnAddPDF.getStyleClass().removeAll("loading-button");
                    btnAddPDF.getStyleClass().add("success-button");

                    // Reset button after 2 seconds
                    Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(2), resetEvent -> {
                        btnAddPDF.setText(originalText);
                        btnAddPDF.getStyleClass().removeAll("success-button");
                        btnAddPDF.getStyleClass().add("secondary-button");
                        btnAddPDF.setDisable(false);
                    }));
                    resetTimer.play();

                } catch (Exception e) {
                    btnAddPDF.setText("❌ Load Failed");
                    btnAddPDF.getStyleClass().removeAll("loading-button");
                    btnAddPDF.getStyleClass().add("error-button");
                    btnAddPDF.setDisable(false);

                    Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(2), resetEvent -> {
                        btnAddPDF.setText(originalText);
                        btnAddPDF.getStyleClass().removeAll("error-button");
                        btnAddPDF.getStyleClass().add("secondary-button");
                    }));
                    resetTimer.play();

                    e.printStackTrace();
                }
            }
        });

        btnTable.setOnAction(e -> {
            System.out.println("Opening topics table.");
            
            // Create new stage for table
            Stage stageTable = new Stage();
            stageTable.setTitle("All Topics");
            
            // Get all topics from database
            List<TopicTableData> allTopics = database.getAllTopics();
            ObservableList<TopicTableData> data = FXCollections.observableArrayList(allTopics);
            
            // Create table
            TableView<TopicTableData> table = new TableView<>();
            table.setItems(data);
            table.setEditable(false);
            
            // Create columns
            TableColumn<TopicTableData, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(60);
            
            TableColumn<TopicTableData, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            typeCol.setPrefWidth(70);
            
            TableColumn<TopicTableData, String> titleCol = new TableColumn<>("Title");
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleCol.setPrefWidth(250);
            
            TableColumn<TopicTableData, String> addedCol = new TableColumn<>("Added");
            addedCol.setCellValueFactory(new PropertyValueFactory<>("addedDate"));
            addedCol.setPrefWidth(120);
            
            TableColumn<TopicTableData, String> scheduledCol = new TableColumn<>("Scheduled");
            scheduledCol.setCellValueFactory(new PropertyValueFactory<>("scheduledDate"));
            scheduledCol.setPrefWidth(120);
            
            TableColumn<TopicTableData, Double> priorityCol = new TableColumn<>("Priority");
            priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
            priorityCol.setPrefWidth(80);
            
            TableColumn<TopicTableData, Double> aFactorCol = new TableColumn<>("A-Factor");
            aFactorCol.setCellValueFactory(new PropertyValueFactory<>("aFactor"));
            aFactorCol.setPrefWidth(80);
            
            // Add columns to table
            table.getColumns().addAll(idCol, typeCol, titleCol, addedCol, scheduledCol, priorityCol, aFactorCol);
            
            // Add double-click functionality to open topics
            table.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    TopicTableData selectedTopic = table.getSelectionModel().getSelectedItem();
                    if (selectedTopic != null) {
                        System.out.println("Opening topic ID: " + selectedTopic.getId());
                        // Find and display the topic
                        Topic topic = database.findTopic(selectedTopic.getId());
                        if (topic != null) {
                            currentTopic = topic;
                            openTopicWindow();
                        }
                    }
                }
            });
            
            // Update window title with count
            stageTable.setTitle("All Topics (" + allTopics.size() + " items)");
            
            // Create layout
            VBox vboxTable = new VBox();
            vboxTable.setSpacing(5);
            vboxTable.setPadding(new Insets(10));
            vboxTable.getChildren().add(table);
            
            // Set up scene and show
            Scene scene = new Scene(vboxTable, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stageTable.setScene(scene);
            stageTable.show();
        });

        btnNext.setOnAction(e -> openTopicWindow());

        btnTopicWithId.setOnAction(e -> {
            String inputText = txtInput.getText().trim();
            if (inputText.isEmpty()) {
                txtInput.getStyleClass().add("error");
                txtInput.setPromptText("Please enter a topic ID");
                return;
            }
            
            try {
                int topicId = Integer.parseInt(inputText);
                currentTopic = database.findTopic(topicId);
                if (currentTopic == null) {
                    txtInput.getStyleClass().add("error");
                    txtInput.setPromptText("Topic not found");
                    txtInput.clear();
                    return;
                }
            } catch (NumberFormatException ex) {
                txtInput.getStyleClass().add("error");
                txtInput.setPromptText("Please enter a valid number");
                txtInput.clear();
                return;
            }
            
            // Clear any error styling and open the topic
            txtInput.getStyleClass().remove("error");
            txtInput.clear();
            openTopicWindow();
        });

        // Apply CSS styling and create scene
        Scene scene = new Scene(mainContainer, 800, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        stage.setTitle("Incremental Learning Application");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(600);
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
            
            // Update page display if this is a PDF topic
            if (currentTopic.isPdf() && pageNumberField != null) {
                try {
                    PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDF(currentTopic.getPdfPath());
                    pageNumberField.setText(String.valueOf(currentTopic.getCurrentPage()));
                    if (totalPagesLabel != null) {
                        totalPagesLabel.setText("of " + pdfInfo.getTotalPages());
                    }
                } catch (Exception ex) {
                    System.err.println("Error updating page display: " + ex.getMessage());
                }
            }
        } else {
            System.out.println("No more topics available.");
        }
    }

    private void openTopicWindow() {
        if (currentTopic == null) {
            System.out.println("No topic to display.");
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
            addPDFControls();
        }

        currentButtonBox.setSpacing(10);
        currentButtonBox.setPadding(new Insets(10));

        currentRichTextArea = new RichTextArea();
        // Load existing content using the database method
        database.loadContentIntoRichTextArea(currentTopic.getContent(), currentRichTextArea);

        // Setup auto-save functionality
        setupAutoSave();

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

        System.out.println("Topic window opened.");
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
        if (currentButtonBox == null)
            return;

        // Remove any existing PDF controls (everything after the first 2
        // buttons: Next Item and Close)
        if (currentButtonBox.getChildren().size() > 2) {
            currentButtonBox.getChildren()
                    .subList(2, currentButtonBox.getChildren().size())
                    .clear();
        }

        // Add PDF controls if this is a PDF topic
        if (currentTopic != null && currentTopic.isPdf()) {
            addPDFControls();
        }
    }

    private void addPDFControls() {
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

    private void setupAutoSave() {
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