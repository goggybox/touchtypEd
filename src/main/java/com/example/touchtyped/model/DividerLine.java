package com.example.touchtyped.model;

import com.example.touchtyped.constants.StyleConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.InputStream;

public class DividerLine {

    /**
     * Creates a divider line with text in the middle
     *
     * @param text the text to display in the middle of the divider line
     * @return an HBox container containing the divider lines and text
     */
    public static HBox createDividerLineWithText(String text) {
        int lineWidth = 5;
        double totalWidth = 730.0; // The total width of the HBox (lines + text + spacing)
        double spacing = 10.0; // The spacing between the lines and text

        if (!text.isEmpty()) {

            // Create the text
            Text dividerText = new Text(text);

            // Load the Antipasto_extrabold font at some initial size
            Font antipastoFont = loadSafeFont("/fonts/Antipasto_extrabold.otf", 40);
            dividerText.setFont(antipastoFont);
            dividerText.setFill(Color.web(StyleConstants.GREY_COLOUR));
            dividerText.setTranslateY(-2);

            // Adjust and truncate if needed
            adjustFontSize(dividerText, 530, 40, 26);

            // get width of the text
            double textWidth = dividerText.getLayoutBounds().getWidth();

            // calculate the available width for the lines
            double remainingWidth = totalWidth - textWidth - (2 * spacing);

            // ensure the remaining width is not negative
            if (remainingWidth < 0) {
                throw new IllegalArgumentException("Text is too long to fit within the total width of 530px");
            }

            // calculate the width of each line
            double lineWidthValue = remainingWidth / 2;

            // create the left line
            Line leftLine = new Line(0, 0, lineWidthValue, 0);
            leftLine.setStroke(Color.web(StyleConstants.GREY_COLOUR));
            leftLine.setStrokeWidth(lineWidth);
            leftLine.setStrokeLineCap(StrokeLineCap.ROUND);

            // create the right line
            Line rightLine = new Line(0, 0, lineWidthValue, 0);
            rightLine.setStroke(Color.web(StyleConstants.GREY_COLOUR));
            rightLine.setStrokeWidth(lineWidth);
            rightLine.setStrokeLineCap(StrokeLineCap.ROUND);

            // create an HBox to hold the lines and text
            HBox dividerBox = new HBox(spacing);
            dividerBox.setAlignment(Pos.CENTER);
            dividerBox.setPadding(new Insets(35, 0, 35, 0));
            dividerBox.getChildren().addAll(leftLine, dividerText, rightLine);

            return dividerBox;
        } else {
            // just create a divider line with no text
            Line dividerLine = new Line(0, 0, totalWidth, 0);
            dividerLine.setStroke(Color.web(StyleConstants.GREY_COLOUR));
            dividerLine.setStrokeWidth(lineWidth);
            dividerLine.setStrokeLineCap(StrokeLineCap.ROUND);

            HBox dividerBox = new HBox();
            dividerBox.setAlignment(Pos.CENTER);
            dividerBox.setPadding(new Insets(35, 0, 35, 0));
            dividerBox.getChildren().add(dividerLine);

            return dividerBox;
        }
    }


    private static void adjustFontSize(Text text, double maxWidth, int maxFontSize, int minFontSize) {
        String content = text.getText();
        Font font = loadSafeFont("/fonts/Antipasto_extrabold.otf", maxFontSize);
        text.setFont(font);

        // Start with the maximum font size and decrease until the text fits or we hit the minimum size
        while (text.getLayoutBounds().getWidth() > maxWidth && font.getSize() > minFontSize) {
            font = loadSafeFont("/fonts/Antipasto_extrabold.otf", font.getSize() - 1);
            text.setFont(font);
        }
    }

    /**
     * Safely loads a font from the given path at the specified size.
     * Falls back to a system font if the resource is not found or fails to load.
     */
    private static Font loadSafeFont(String resourcePath, double size) {
        try (InputStream fontStream = DividerLine.class.getResourceAsStream(resourcePath)) {
            if (fontStream != null) {
                Font loadedFont = Font.loadFont(fontStream, size);
                if (loadedFont != null) {
                    return loadedFont;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback to default system font if loading fails
        return Font.font("System", size);
    }

}
