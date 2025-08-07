package com.adham_omran;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import jfx.incubator.scene.control.richtext.RichTextArea;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;

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
    private Label currentSourceLabel;
    private Button currentViewSourceButton;
    private Button currentOpenTopicButton;
    private HBox currentButtonBox;
    private TextField pageNumberField;
    private Label totalPagesLabel;
    private double currentZoomLevel = 1.0;
    private Label zoomLevelLabel;
    private boolean drawMode = false;
    private Canvas drawingCanvas;
    private StackPane imageContainer;
    private double drawStartX, drawStartY;
    private boolean isDragging = false;

    @Override
    public void start(Stage stage) throws IOException {
        // Layout

        // Initialize database
        database = new Database();

        // Create main container
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
            ButtonStateManager.showLoadingState(btnClipboard, "💾 Saving...");

            BufferedImage bufferedImage;
            java.awt.Image awtImage = cp.getImageFromClipboard();

            // Check if clipboard contains an image
            if (awtImage == null) {
                ButtonStateManager.showErrorState(btnClipboard, "❌ No image in clipboard", 3.0);
                return;
            }

            // Convert to BufferedImage using ImageUtils
            bufferedImage = ImageUtils.awtImageToBufferedImage(awtImage);
            if (bufferedImage != null) {
                System.out.println("Image conversion successful.");
            } else {
                ButtonStateManager.showErrorState(btnClipboard, "❌ Image conversion failed", 3.0);
                return;
            }
            // Save the image using ImageUtils
            InputStream imageStream = ImageUtils.bufferedImageToInputStream(bufferedImage);
            int imageSize = ImageUtils.getBufferedImageByteSize(bufferedImage);

            if (imageStream != null && imageSize > 0) {
                dbDatabase.saveImage(imageStream, imageSize);
                ButtonStateManager.showSuccessState(btnClipboard, "✅ Saved!", 2.0);
            } else {
                ButtonStateManager.showErrorState(btnClipboard, "❌ Save failed", 3.0);
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
                ButtonStateManager.showLoadingState(btnAddPDF, "📥 Loading...");

                try {
                    dbDatabase.savePDF(selectedFile.getAbsolutePath());
                    ButtonStateManager.showSuccessState(btnAddPDF, "✅ PDF Loaded!", 2.0);

                } catch (Exception e) {
                    ButtonStateManager.showErrorState(btnAddPDF, "❌ Load Failed", 2.0);
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

        btnNext.setOnAction(e -> {
            // Initialize currentTopic if it's null
            if (currentTopic == null) {
                currentTopic = database.nextTopic();
                if (currentTopic == null) {
                    System.out.println("No topics available in the database.");
                    return;
                }
            }
            openTopicWindow();
        });

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
        Scene scene = new Scene(mainContainer, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Incremental");
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
            displayTopic(currentTopic);
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
        currentImageView.setFitWidth(600);
        currentImageView.setPreserveRatio(true);

        // Initialize drawing canvas
        drawingCanvas = new Canvas();
        drawingCanvas.setMouseTransparent(true);

        // Create container for layering ImageView and Canvas
        imageContainer = new StackPane();
        imageContainer.getChildren().addAll(currentImageView, drawingCanvas);

        // Reset zoom level for new topic
        currentZoomLevel = 1.0;

        currentScrollPane = new ScrollPane();
        currentScrollPane.setContent(imageContainer);
        currentScrollPane.setFitToWidth(true);
        currentScrollPane.setFitToHeight(true);

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
        btnNextItem.getStyleClass().add("primary-button");
        btnNextItem.setOnAction(this::handleNextItem);

        Button btnClose = new Button("Close");
        btnClose.getStyleClass().add("secondary-button");
        btnClose.setOnAction(this::handleClose);

        currentButtonBox = new HBox();

        // Create main controls section for Next Item and Close buttons
        VBox mainControlsSection = new VBox();
        mainControlsSection.getStyleClass().add("section-container");
        mainControlsSection.setSpacing(8);

        Label mainLabel = new Label("Topic Controls");
        mainLabel.getStyleClass().add("section-title");

        HBox mainButtons = new HBox();
        mainButtons.getStyleClass().add("button-group");
        mainButtons.getChildren().addAll(btnNextItem, btnClose);

        mainControlsSection.getChildren().addAll(mainLabel, mainButtons);
        currentButtonBox.getChildren().add(mainControlsSection);

        // Add PDF controls if this is a PDF topic, otherwise add image controls for
        // regular images and extracts
        if (currentTopic.isPdf()) {
            addPDFControls();
        } else if (currentImageView.getImage() != null) {
            // Add image controls for regular images and extract topics
            addImageControls();
        }

        currentButtonBox.setSpacing(6);
        currentButtonBox.setPadding(new Insets(8));

        currentRichTextArea = new RichTextArea();

        // Setup auto-save functionality
        setupAutoSave();

        // Create source info box
        VBox sourceInfoBox = createSourceInfoBox();

        // Load and display the topic content (this will handle image loading, content
        // loading, and UI updates)
        displayTopic(currentTopic);

        VBox vboxItem = new VBox();
        vboxItem.getChildren().addAll(
                currentButtonBox,
                currentScrollPane,
                currentRichTextArea,
                sourceInfoBox);
        vboxItem.setSpacing(10);
        vboxItem.setPadding(new Insets(10));

        Scene itemScene = new Scene(vboxItem, 800, 700);
        itemScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        itemStage.setScene(itemScene);

        // Bind currentScrollPane max height to 80% of stage height
        currentScrollPane.maxHeightProperty().bind(itemStage.heightProperty().multiply(0.8));

        itemStage.show();

        Platform.runLater(() -> updateCanvasSize());

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

    private void displayTopic(Topic topic) {
        if (topic == null) {
            System.out.println("No topic to display.");
            return;
        }

        // Load and render image/PDF
        Image img = null;
        if (topic.isPdf()) {
            // Render PDF page
            try {
                PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(topic.getPdfPath());
                if (pdfInfo == null) return;
                img = PDFImageRenderer.renderPageToFXImage(pdfInfo, topic.getCurrentPage());
                System.out.println("Rendered PDF page " + topic.getCurrentPage());
            } catch (Exception ex) {
                System.err.println("Error rendering PDF: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            img = topic.getTopicImage();
        }

        // Set image on currentImageView (if it exists)
        if (currentImageView != null && img != null) {
            currentImageView.setImage(img);
            if (currentScrollPane != null) {
                currentScrollPane.setVisible(true);
                currentScrollPane.setManaged(true);
            }
            System.out.println("Image loaded for topic: " + topic.getRowId());
        } else if (currentImageView != null && img == null) {
            // Topic has no image - hide the scroll pane
            if (currentScrollPane != null) {
                currentScrollPane.setVisible(false);
                currentScrollPane.setManaged(false);
            }
            System.out.println("No image to display for topic: " + topic.getRowId());
        }

        // Load content into RichTextArea (if it exists)
        if (currentRichTextArea != null) {
            database.loadContentIntoRichTextArea(topic.getContent(), currentRichTextArea);
        }

        // Update controls based on the new topic
        updateTopicControls();

        // Update source info box
        updateSourceInfo();

        // Update page display if this is a PDF topic
        if (topic.isPdf() && pageNumberField != null) {
            try {
                PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(topic.getPdfPath());
                if (pdfInfo == null) return;
                pageNumberField.setText(String.valueOf(topic.getCurrentPage()));
                if (totalPagesLabel != null) {
                    totalPagesLabel.setText("of " + pdfInfo.getTotalPages());
                }
            } catch (Exception ex) {
                System.err.println("Error updating page display: " + ex.getMessage());
            }
        }

        // Update canvas size for drawing
        Platform.runLater(() -> updateCanvasSize());
    }

    private Button[] createPageNavigationButtons(boolean isCompact) {
        Button btnPrevPage = new Button("◀ Prev");
        btnPrevPage.getStyleClass().add(isCompact ? "compact-secondary-button" : "secondary-button");

        Button btnNextPage = new Button("Next ▶");
        btnNextPage.getStyleClass().add(isCompact ? "compact-secondary-button" : "secondary-button");

        // Return array: [prevButton, nextButton]
        return new Button[]{btnPrevPage, btnNextPage};
    }

    private VBox createZoomControls() {
        // Create view options section
        VBox viewSection = new VBox();
        viewSection.getStyleClass().add("compact-section-container");
        viewSection.setSpacing(4);

        Label viewLabel = new Label("View Options");
        viewLabel.getStyleClass().add("compact-section-title");

        // Preset fit buttons
        HBox fitBox = new HBox();
        fitBox.getStyleClass().add("compact-button-group");

        Button btnFitToPage = new Button("Fit Page");
        btnFitToPage.getStyleClass().add("compact-tertiary-button");
        Button btnFitToWidth = new Button("Fit Width");
        btnFitToWidth.getStyleClass().add("compact-tertiary-button");

        fitBox.getChildren().addAll(btnFitToPage, btnFitToWidth);

        // Zoom controls
        HBox zoomBox = new HBox();
        zoomBox.getStyleClass().add("compact-form-group");
        zoomBox.setSpacing(4);

        Button btnZoomOut = new Button("➖");
        btnZoomOut.getStyleClass().add("compact-secondary-button");
        btnZoomOut.setTooltip(new Tooltip("Zoom out (Ctrl + -)"));

        Button btnZoomIn = new Button("➕");
        btnZoomIn.getStyleClass().add("compact-secondary-button");
        btnZoomIn.setTooltip(new Tooltip("Zoom in (Ctrl + +)"));

        zoomLevelLabel = new Label(Math.round(currentZoomLevel * 100) + "%");
        zoomLevelLabel.setMinWidth(40);
        zoomLevelLabel.getStyleClass().add("compact-section-title");

        Button btnZoomReset = new Button("Reset");
        btnZoomReset.getStyleClass().add("compact-tertiary-button");
        btnZoomReset.setTooltip(new Tooltip("Reset zoom to 100%"));

        zoomBox.getChildren().addAll(btnZoomOut, zoomLevelLabel, btnZoomIn, btnZoomReset);

        viewSection.getChildren().addAll(viewLabel, fitBox, zoomBox);

        // Set up event handlers
        btnFitToPage.setOnAction(event -> {
            currentZoomLevel = Math.min(
                    (currentScrollPane.getWidth() - 20) / currentImageView.getImage().getWidth(),
                    (currentScrollPane.getHeight() - 20) / currentImageView.getImage().getHeight());
            applyZoom();
            System.out.println("Fit to page");
        });

        btnFitToWidth.setOnAction(event -> {
            currentZoomLevel = (currentScrollPane.getWidth() - 20) / currentImageView.getImage().getWidth();
            applyZoom();
            System.out.println("Fit to width");
        });

        // Zoom controls
        btnZoomIn.setOnAction(event -> {
            currentZoomLevel = Math.min(currentZoomLevel * 1.25, 5.0); // Max 500%
            applyZoom();
        });

        btnZoomOut.setOnAction(event -> {
            currentZoomLevel = Math.max(currentZoomLevel / 1.25, 0.1); // Min 10%
            applyZoom();
        });

        btnZoomReset.setOnAction(event -> {
            currentZoomLevel = 1.0;
            applyZoom();
        });

        return viewSection;
    }

    private void handleOpenSourceAsTopic(ActionEvent e) {
        if (currentTopic == null || currentTopic.getTopicParent() <= 0) {
            System.err.println("Cannot view source: no parent topic available");
            return;
        }

        try {
            Topic parentTopic = database.findTopic(currentTopic.getTopicParent());
            if (parentTopic != null) {
                // Save current content before switching
                if (saveTimer != null) {
                    saveTimer.stop();
                }
                if (currentTopic != null && currentRichTextArea != null) {
                    database.updateContent(currentTopic.getRowId(), currentRichTextArea);
                }

                // Switch to parent topic
                currentTopic = parentTopic;

                // Update the UI to show the parent PDF
                openTopicWindow();

                System.out.println("Opened source PDF for parent topic: " + parentTopic.getRowId());
            } else {
                System.err.println("Parent topic not found: " + currentTopic.getTopicParent());
            }
        } catch (Exception ex) {
            System.err.println("Error opening source PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleViewSourcePdf(ActionEvent e) {
        if (currentTopic == null || currentTopic.getTopicParent() <= 0) {
            System.err.println("Cannot view source: no parent topic available");
            return;
        }

        try {
            Topic parentTopic = database.findTopic(currentTopic.getTopicParent());
            if (parentTopic != null && parentTopic.isPdf()) {
                openLightweightPdfViewer(parentTopic);
                System.out.println("Opened lightweight PDF viewer for parent topic: " + parentTopic.getRowId());
            } else {
                System.err.println("Parent topic not found or not a PDF: " + currentTopic.getTopicParent());
            }
        } catch (Exception ex) {
            System.err.println("Error opening PDF viewer: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openLightweightPdfViewer(Topic pdfTopic) {
        Stage viewerStage = new Stage();
        viewerStage.setTitle("Source PDF - " + getFileNameFromPath(pdfTopic.getPdfPath()));

        // Create image view for PDF display
        ImageView pdfImageView = new ImageView();

        // Render the PDF page
        Image pdfImg = null;
        try {
            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(pdfTopic.getPdfPath());
            if (pdfInfo == null) return;
            pdfImg = PDFImageRenderer.renderPageToFXImage(pdfInfo, pdfTopic.getCurrentPage());
            System.out.println("Rendered PDF page " + pdfTopic.getCurrentPage() + " in viewer");
        } catch (Exception ex) {
            System.err.println("Error rendering PDF in viewer: " + ex.getMessage());
            ex.printStackTrace();
        }

        if (pdfImg != null) {
            pdfImageView.setImage(pdfImg);
            pdfImageView.setFitWidth(700);
            pdfImageView.setPreserveRatio(true);
        }

        // Create simple page controls using shared navigation
        Button[] navButtons = createPageNavigationButtons(true); // compact style
        Button btnPrevPage = navButtons[0];
        Button btnNextPage = navButtons[1];

        Label pageLabel = new Label("Page " + pdfTopic.getCurrentPage());

        HBox pageControls = new HBox();
        pageControls.getStyleClass().add("button-group");
        pageControls.setSpacing(4);
        pageControls.getChildren().addAll(btnPrevPage, pageLabel, btnNextPage);

        // Simple page navigation handlers
        btnPrevPage.setOnAction(e -> {
            if (pdfTopic.getCurrentPage() > 1) {
                pdfTopic.setCurrentPage(pdfTopic.getCurrentPage() - 1);
                updatePdfViewer(pdfImageView, pdfTopic, pageLabel);
            }
        });

        btnNextPage.setOnAction(e -> {
            pdfTopic.setCurrentPage(pdfTopic.getCurrentPage() + 1);
            updatePdfViewer(pdfImageView, pdfTopic, pageLabel);
        });

        // Create scroll pane for the PDF
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(pdfImageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Layout
        VBox viewerLayout = new VBox();
        viewerLayout.setSpacing(10);
        viewerLayout.setPadding(new Insets(10));
        viewerLayout.getChildren().addAll(pageControls, scrollPane);

        Scene viewerScene = new Scene(viewerLayout, 750, 600);
        viewerScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        viewerStage.setScene(viewerScene);
        viewerStage.show();
    }

    private void updatePdfViewer(ImageView imageView, Topic pdfTopic, Label pageLabel) {
        try {
            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(pdfTopic.getPdfPath());
            if (pdfInfo == null) return;
            Image img = PDFImageRenderer.renderPageToFXImage(pdfInfo, pdfTopic.getCurrentPage());
            imageView.setImage(img);
            pageLabel.setText("Page " + pdfTopic.getCurrentPage());
            System.out.println("Updated PDF viewer to page " + pdfTopic.getCurrentPage());
        } catch (Exception ex) {
            System.err.println("Error updating PDF viewer: " + ex.getMessage());
        }
    }

    private void updateTopicControls() {
        if (currentButtonBox == null)
            return;

        // Remove any existing controls (everything after the first section
        // which contains the main Topic Controls)
        if (currentButtonBox.getChildren().size() > 1) {
            currentButtonBox.getChildren()
                    .subList(1, currentButtonBox.getChildren().size())
                    .clear();
        }

        // Add appropriate controls based on topic type
        if (currentTopic != null && currentTopic.isPdf()) {
            addPDFControls();
        } else if (currentTopic != null && currentImageView.getImage() != null) {
            addImageControls();
        }
    }

    private void addPDFControls() {
        try {
            // Get PDF info for total pages
            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(currentTopic.getPdfPath());
            if (pdfInfo == null) return;
            int totalPages = pdfInfo.getTotalPages();

            // Create navigation section
            VBox navigationSection = new VBox();
            navigationSection.getStyleClass().add("compact-section-container");
            navigationSection.setSpacing(4);

            Label navLabel = new Label("Page Navigation");
            navLabel.getStyleClass().add("compact-section-title");

            // Page navigation controls using shared navigation
            Button[] navButtons = createPageNavigationButtons(true); // compact style
            Button btnPrevPage = navButtons[0];
            Button btnNextPage = navButtons[1];

            HBox pageNavBox = new HBox();
            pageNavBox.getStyleClass().add("compact-button-group");
            pageNavBox.getChildren().addAll(btnPrevPage, btnNextPage);

            // Page number display and jump
            HBox pageInfoBox = new HBox();
            pageInfoBox.getStyleClass().add("compact-form-group");
            pageInfoBox.setSpacing(4);

            Label pageLabel = new Label("Page:");
            pageNumberField = new TextField(String.valueOf(currentTopic.getCurrentPage()));
            pageNumberField.getStyleClass().add("compact-text-field");
            pageNumberField.setPrefWidth(50);
            pageNumberField.setTooltip(new Tooltip("Enter page number and press Enter to jump"));

            totalPagesLabel = new Label("of " + totalPages);

            Button btnJumpToPage = new Button("Go");
            btnJumpToPage.getStyleClass().add("compact-tertiary-button");
            btnJumpToPage.setTooltip(new Tooltip("Jump to the specified page"));

            pageInfoBox.getChildren().addAll(pageLabel, pageNumberField, totalPagesLabel, btnJumpToPage);

            navigationSection.getChildren().addAll(navLabel, pageNavBox, pageInfoBox);

            // Create view options section using shared zoom controls
            VBox viewSection = createZoomControls();

            // Create drawing section
            VBox drawingSection = new VBox();
            drawingSection.getStyleClass().add("compact-section-container");
            drawingSection.setSpacing(4);

            Label drawLabel = new Label("Drawing Tools");
            drawLabel.getStyleClass().add("compact-section-title");

            HBox drawBox = new HBox();
            drawBox.getStyleClass().add("compact-button-group");

            Button btnDrawMode = new Button("Draw Mode");
            btnDrawMode.getStyleClass().add("compact-tertiary-button");
            btnDrawMode.setTooltip(new Tooltip("Toggle drawing mode to draw rectangles"));

            Button btnClearRects = new Button("Clear");
            btnClearRects.getStyleClass().add("compact-secondary-button");
            btnClearRects.setTooltip(new Tooltip("Clear all rectangles on this page"));

            drawBox.getChildren().addAll(btnDrawMode, btnClearRects);
            drawingSection.getChildren().addAll(drawLabel, drawBox);

            // Add sections to main button box
            currentButtonBox.getChildren().addAll(navigationSection, viewSection, drawingSection);

            // Set up event handlers
            btnPrevPage.setOnAction(event -> navigateToPage(currentTopic.getCurrentPage() - 1));
            btnNextPage.setOnAction(event -> navigateToPage(currentTopic.getCurrentPage() + 1));

            // Page jump functionality
            btnJumpToPage.setOnAction(event -> jumpToPage());
            pageNumberField.setOnAction(event -> jumpToPage());

            // Drawing controls
            btnDrawMode.setOnAction(event -> toggleDrawMode(btnDrawMode));
            btnClearRects.setOnAction(event -> clearRectangles());

        } catch (Exception ex) {
            System.err.println("Error setting up PDF controls: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addImageControls() {
        try {
            // Use shared zoom controls for images
            VBox viewSection = createZoomControls();

            // Add section to main button box
            currentButtonBox.getChildren().add(viewSection);

        } catch (Exception ex) {
            System.err.println("Error setting up image controls: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void navigateToPage(int newPage) {
        try {
            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(currentTopic.getPdfPath());
            if (pdfInfo == null) return;

            // Validate page bounds using ValidationUtils
            if (!ValidationUtils.validatePageNumber(newPage, pdfInfo.getTotalPages(), "page jump")) {
                return;
            }

            // Update topic and database
            currentTopic.setCurrentPage(newPage);
            database.updatePDFPage(currentTopic.getRowId(), newPage);

            // Re-display the topic with the new page
            displayTopic(currentTopic);

            // Apply current zoom level to new page
            applyZoom();

            // Scroll to top of the new page for natural reading flow
            Platform.runLater(() -> {
                if (currentScrollPane != null) {
                    currentScrollPane.setVvalue(0.0); // Scroll to top
                    currentScrollPane.setHvalue(0.0); // Scroll to left
                }
            });

            System.out.println("Moved to page " + newPage + " of " + pdfInfo.getTotalPages());

        } catch (Exception ex) {
            System.err.println("Error navigating to page " + newPage + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void jumpToPage() {
        try {
            String pageText = pageNumberField.getText().trim();
            if (pageText.isEmpty()) {
                pageNumberField.getStyleClass().add("error");
                pageNumberField.setPromptText("Enter page number");
                return;
            }

            int targetPage = Integer.parseInt(pageText);
            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(currentTopic.getPdfPath());
            if (pdfInfo == null) return;

            if (targetPage < 1 || targetPage > pdfInfo.getTotalPages()) {
                pageNumberField.getStyleClass().add("error");
                pageNumberField.setText(String.valueOf(currentTopic.getCurrentPage()));
                pageNumberField.setPromptText("Page must be between 1 and " + pdfInfo.getTotalPages());
                return;
            }

            // Clear any error styling
            pageNumberField.getStyleClass().remove("error");

            // Navigate to the page
            navigateToPage(targetPage);

        } catch (NumberFormatException ex) {
            pageNumberField.getStyleClass().add("error");
            pageNumberField.setText(String.valueOf(currentTopic.getCurrentPage()));
            pageNumberField.setPromptText("Please enter a valid number");
        } catch (Exception ex) {
            System.err.println("Error jumping to page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void applyZoom() {
        if (currentImageView != null && currentImageView.getImage() != null) {
            // Apply zoom by setting fixed dimensions
            double originalWidth = currentImageView.getImage().getWidth();
            double originalHeight = currentImageView.getImage().getHeight();

            currentImageView.setPreserveRatio(true);
            currentImageView.setFitWidth(originalWidth * currentZoomLevel);
            currentImageView.setFitHeight(originalHeight * currentZoomLevel);

            // Disable scroll pane auto-fitting to allow custom sizing
            currentScrollPane.setFitToWidth(false);
            currentScrollPane.setFitToHeight(false);

            // Update zoom label
            if (zoomLevelLabel != null) {
                zoomLevelLabel.setText(Math.round(currentZoomLevel * 100) + "%");
            }

            System.out.println("Applied zoom: " + Math.round(currentZoomLevel * 100) + "%");

            updateCanvasSize();
        }
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

    private void toggleDrawMode(Button btnDrawMode) {
        drawMode = !drawMode;
        if (drawMode) {
            btnDrawMode.setText("Exit Draw");
            btnDrawMode.getStyleClass().removeAll("compact-tertiary-button");
            btnDrawMode.getStyleClass().add("compact-secondary-button");
            enableDrawing();
        } else {
            btnDrawMode.setText("Draw Mode");
            btnDrawMode.getStyleClass().removeAll("compact-secondary-button");
            btnDrawMode.getStyleClass().add("compact-tertiary-button");
            disableDrawing();
        }
        System.out.println("Draw mode: " + (drawMode ? "ON" : "OFF"));
    }

    private void enableDrawing() {
        drawingCanvas.setMouseTransparent(false);
        setupDrawingHandlers();
    }

    private void disableDrawing() {
        drawingCanvas.setMouseTransparent(true);
        drawingCanvas.setOnMousePressed(null);
        drawingCanvas.setOnMouseDragged(null);
        drawingCanvas.setOnMouseReleased(null);
    }

    private void setupDrawingHandlers() {
        drawingCanvas.setOnMousePressed(this::onDrawingMousePressed);
        drawingCanvas.setOnMouseDragged(this::onDrawingMouseDragged);
        drawingCanvas.setOnMouseReleased(this::onDrawingMouseReleased);
    }

    private void onDrawingMousePressed(MouseEvent event) {
        drawStartX = event.getX();
        drawStartY = event.getY();
        isDragging = true;
        System.out.println("Started drawing at: " + drawStartX + ", " + drawStartY);
    }

    private void onDrawingMouseDragged(MouseEvent event) {
        if (isDragging) {
            drawPreviewRectangle(drawStartX, drawStartY, event.getX(), event.getY());
        }
    }

    private void onDrawingMouseReleased(MouseEvent event) {
        if (isDragging) {
            isDragging = false;
            saveRectangle(drawStartX, drawStartY, event.getX(), event.getY());
            refreshRectangles();
            System.out.println("Finished drawing at: " + event.getX() + ", " + event.getY());
        }
    }

    private void drawPreviewRectangle(double x1, double y1, double x2, double y2) {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        redrawExistingRectangles();

        gc.setStroke(Color.RED);
        gc.setLineWidth(2.0);
        gc.strokeRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    private void saveRectangle(double screenX1, double screenY1, double screenX2, double screenY2) {
        if (currentTopic == null || !currentTopic.isPdf())
            return;

        if (currentImageView.getImage() == null)
            return;

        double imageWidth = currentImageView.getBoundsInLocal().getWidth();
        double imageHeight = currentImageView.getBoundsInLocal().getHeight();

        double relX1 = screenX1 / imageWidth;
        double relY1 = screenY1 / imageHeight;
        double relX2 = screenX2 / imageWidth;
        double relY2 = screenY2 / imageHeight;

        RectangleData rect = new RectangleData(
                currentTopic.getRowId(),
                currentTopic.getCurrentPage(),
                relX1, relY1, relX2, relY2);

        database.saveRectangle(rect);

        // Automatically extract content from the rectangle
        extractAndSaveContent(relX1, relY1, relX2, relY2);
    }

    private void extractAndSaveContent(double relX1, double relY1, double relX2, double relY2) {
        if (currentTopic == null || !currentTopic.isPdf())
            return;

        ErrorHandler.executeWithErrorHandling("extracting and saving content", () -> {
            // Load PDF for extraction
            PDFImageRenderer.PDFInfo pdfInfo = PDFImageRenderer.loadPDFSafe(currentTopic.getPdfPath());
            if (pdfInfo == null) {
                System.err.println("Failed to load PDF for extraction");
                return;
            }

            // Extract rectangle content
            BufferedImage extractedImage = PDFImageRenderer.extractRectangleFromPDF(
                    pdfInfo,
                    currentTopic.getCurrentPage(),
                    relX1, relY1, relX2, relY2);

            if (extractedImage != null) {
                // Convert to InputStream for database storage using ImageUtils
                InputStream imageStream = ImageUtils.bufferedImageToInputStream(extractedImage);
                int imageSize = ImageUtils.getBufferedImageByteSize(extractedImage);

                // Save as new extracted topic
                database.saveExtractedTopic(
                        imageStream,
                        imageSize,
                        currentTopic.getRowId(),
                        currentTopic.getCurrentPage());

                System.out.println("Successfully extracted and saved content from rectangle");
            } else {
                System.err.println("Failed to extract content from rectangle");
            }
        });
    }

    private void clearRectangles() {
        if (currentTopic == null || !currentTopic.isPdf())
            return;

        database.deleteRectanglesForPage(currentTopic.getRowId(), currentTopic.getCurrentPage());
        refreshRectangles();
        System.out.println("Cleared rectangles for current page");
    }

    private void refreshRectangles() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        redrawExistingRectangles();
    }

    private void redrawExistingRectangles() {
        if (currentTopic == null || !currentTopic.isPdf())
            return;

        List<RectangleData> rectangles = database.getRectanglesForPage(
                currentTopic.getRowId(),
                currentTopic.getCurrentPage());

        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2.0);

        double imageWidth = currentImageView.getBoundsInLocal().getWidth();
        double imageHeight = currentImageView.getBoundsInLocal().getHeight();

        for (RectangleData rect : rectangles) {
            double x1 = rect.getX1() * imageWidth;
            double y1 = rect.getY1() * imageHeight;
            double x2 = rect.getX2() * imageWidth;
            double y2 = rect.getY2() * imageHeight;

            gc.strokeRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
        }
    }

    private void updateCanvasSize() {
        if (drawingCanvas != null && currentImageView != null) {
            drawingCanvas.setWidth(currentImageView.getBoundsInLocal().getWidth());
            drawingCanvas.setHeight(currentImageView.getBoundsInLocal().getHeight());
            refreshRectangles();
        }
    }

    private VBox createSourceInfoBox() {
        VBox infoBox = new VBox();
        infoBox.getStyleClass().add("section-container");
        infoBox.setSpacing(4);

        // Create HBox for label and button layout
        HBox contentBox = new HBox();
        contentBox.setSpacing(8);

        // Create source label (takes 80% of width)
        currentSourceLabel = new Label();
        currentSourceLabel.getStyleClass().add("info-text");
        HBox.setHgrow(currentSourceLabel, Priority.ALWAYS);
        currentSourceLabel.setMaxWidth(Double.MAX_VALUE);

        // Create buttons container for two buttons
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(4);

        // Create "Open for View" button
        currentViewSourceButton = new Button("View");
        currentViewSourceButton.getStyleClass().add("compact-tertiary-button");
        currentViewSourceButton.setOnAction(this::handleViewSourcePdf);
        currentViewSourceButton.setVisible(false);
        currentViewSourceButton.setManaged(false);

        // Create "Open as Topic" button
        currentOpenTopicButton = new Button("Open");
        currentOpenTopicButton.getStyleClass().add("compact-tertiary-button");
        currentOpenTopicButton.setOnAction(this::handleOpenSourceAsTopic);
        currentOpenTopicButton.setVisible(false);
        currentOpenTopicButton.setManaged(false);

        buttonsBox.getChildren().addAll(currentViewSourceButton, currentOpenTopicButton);

        contentBox.getChildren().addAll(currentSourceLabel, buttonsBox);

        // Set initial content
        updateSourceInfo();

        infoBox.getChildren().addAll(contentBox);
        return infoBox;
    }

    private void updateSourceInfo() {
        if (currentSourceLabel == null) {
            return;
        }

        if (currentTopic != null) {
            String kind = currentTopic.getKind();
            if (kind == null)
                kind = "unknown";

            switch (kind.toLowerCase()) {
                case "pdf":
                    if (currentTopic.isPdf()) {
                        String fileName = getFileNameFromPath(currentTopic.getPdfPath());
                        currentSourceLabel
                                .setText("PDF: " + fileName + " (Page " + currentTopic.getCurrentPage() + ")");
                    } else {
                        currentSourceLabel.setText("PDF (no path available)");
                    }
                    // Hide buttons for PDF items
                    if (currentViewSourceButton != null) {
                        currentViewSourceButton.setVisible(false);
                        currentViewSourceButton.setManaged(false);
                    }
                    if (currentOpenTopicButton != null) {
                        currentOpenTopicButton.setVisible(false);
                        currentOpenTopicButton.setManaged(false);
                    }
                    break;
                case "extract":
                    if (currentTopic.getTopicParent() > 0) {
                        String parentPdfPath = database.getParentPdfPath(currentTopic.getTopicParent());
                        if (parentPdfPath != null) {
                            String fileName = getFileNameFromPath(parentPdfPath);
                            String pageInfo = "";
                            if (currentTopic.getPdfPage() != null) {
                                pageInfo = " (Page " + currentTopic.getPdfPage() + ")";
                            }
                            currentSourceLabel.setText("Extract from PDF: " + fileName + pageInfo);
                            // Show both buttons for valid extracts
                            if (currentViewSourceButton != null) {
                                currentViewSourceButton.setVisible(true);
                                currentViewSourceButton.setManaged(true);
                            }
                            if (currentOpenTopicButton != null) {
                                currentOpenTopicButton.setVisible(true);
                                currentOpenTopicButton.setManaged(true);
                            }
                        } else {
                            currentSourceLabel.setText("Extract from PDF (parent topic not found)");
                            // Hide buttons if parent not found
                            if (currentViewSourceButton != null) {
                                currentViewSourceButton.setVisible(false);
                                currentViewSourceButton.setManaged(false);
                            }
                            if (currentOpenTopicButton != null) {
                                currentOpenTopicButton.setVisible(false);
                                currentOpenTopicButton.setManaged(false);
                            }
                        }
                    } else {
                        currentSourceLabel.setText("Extract from PDF (no parent topic)");
                        // Hide buttons if no parent topic
                        if (currentViewSourceButton != null) {
                            currentViewSourceButton.setVisible(false);
                            currentViewSourceButton.setManaged(false);
                        }
                        if (currentOpenTopicButton != null) {
                            currentOpenTopicButton.setVisible(false);
                            currentOpenTopicButton.setManaged(false);
                        }
                    }
                    break;
                case "image":
                default:
                    currentSourceLabel.setText("Image (Topic ID: " + currentTopic.getRowId() + ")");
                    // Hide buttons for image items
                    if (currentViewSourceButton != null) {
                        currentViewSourceButton.setVisible(false);
                        currentViewSourceButton.setManaged(false);
                    }
                    if (currentOpenTopicButton != null) {
                        currentOpenTopicButton.setVisible(false);
                        currentOpenTopicButton.setManaged(false);
                    }
                    break;
            }
        } else {
            currentSourceLabel.setText("No topic loaded");
            // Hide buttons when no topic is loaded
            if (currentViewSourceButton != null) {
                currentViewSourceButton.setVisible(false);
                currentViewSourceButton.setManaged(false);
            }
            if (currentOpenTopicButton != null) {
                currentOpenTopicButton.setVisible(false);
                currentOpenTopicButton.setManaged(false);
            }
        }
    }

    private String getFileNameFromPath(String path) {
        return FileUtils.getFileNameFromPath(path);
    }

    public static Image bufferedImageToFXImage(BufferedImage bufferedImage) {
        return ImageUtils.bufferedImageToFXImage(bufferedImage);
    }

    public static void main(String[] args) {
        launch();
    }

}
