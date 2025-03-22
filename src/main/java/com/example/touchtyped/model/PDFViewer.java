package com.example.touchtyped.model;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class PDFViewer extends VBox {

    public PDFViewer(byte[] pdfData) {
        try (PDDocument document = PDDocument.load(pdfData)) {
            PDFRenderer renderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(page, 96, ImageType.RGB);

                int xMargin = 40;
                int yMargin = 30;
                BufferedImage croppedImage = pageImage.getSubimage(
                        xMargin,
                        yMargin*3,
                        pageImage.getWidth() - 2*xMargin,
                        pageImage.getHeight() - 8*yMargin
                );

                WritableImage fxImage = SwingFXUtils.toFXImage(croppedImage, null);
                ImageView imageView = new ImageView(fxImage);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(575);
                getChildren().add(imageView);
            }

        } catch (IOException e) {
            getChildren().add(new Label("Failed to render PDF."));
            e.printStackTrace();
        }
    }

}
