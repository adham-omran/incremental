package com.adham_omran;

// Old PDF code for showing a pdf page as an image


        // // PDF Loading
        // File pdfFile = new File("/Users/adham/code/incremental-minimal/src/main/java/com/adham_omran/test.pdf");

        // PDDocument doc = Loader.loadPDF(pdfFile);

        // PDFRenderer rndr = new PDFRenderer(doc);

        // // Render at high DPI for quality, then scale down for display
        // BufferedImage highQualityImage = rndr.renderImageWithDPI(0, 300);
        // System.out.println("High quality image class: " + highQualityImage.getClass());
        // System.out.println("Converted FX image class: " + bufferedImageToFXImage(highQualityImage).getClass());

        // // Image Viewing with scaling
        // ImageView iv2 = new ImageView(bufferedImageToFXImage(highQualityImage));

        // // Scale down for display (300 DPI -> 72 DPI equivalent)
        // double scaleFactor = 72.0 / 300.0; // Scale down by ~24%
        // iv2.setFitWidth(highQualityImage.getWidth() * scaleFactor);
        // iv2.setFitHeight(highQualityImage.getHeight() * scaleFactor);
        // iv2.setPreserveRatio(true);
        // iv2.setSmooth(true); // Enable smooth scaling
