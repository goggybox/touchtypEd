package com.example.touchtyped.model;

import com.example.touchtyped.constants.StyleConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;

import java.io.InputStream;

public class ModuleButton {

    /**
     * returns a StackPane that represents a module in the Learn page.
     * @param module is the module to display
     * @param onClickAction is the action to perform when the button is clicked
     * @return a StackPane to be displayed
     */
    public static StackPane createModuleButton(Module module, Runnable onClickAction) {

        String moduleName = module.getName();
        double completion = module.getCompletion();
        int arcRadius = 53;
        int arcWidth = 10;
        int blueCircleRadius = 40;

        Pane pane = new Pane();
        pane.setPrefWidth(250);

        // Safely load AntipastoPro.ttf at initial size 26
        Font antipastoFont = loadSafeFont("/fonts/AntipastoPro.ttf", 26);

        double centerX = pane.getPrefWidth() / 2;
        double centerY = arcRadius + arcWidth / 2;

        boolean locked = module.isLocked();

        // Gray arc (full circle)
        Arc greyArc = new Arc();
        greyArc.setCenterX(centerX);
        greyArc.setCenterY(centerY);
        greyArc.setRadiusX(arcRadius);
        greyArc.setRadiusY(arcRadius);
        greyArc.setLength(360);
        greyArc.setType(ArcType.OPEN);
        greyArc.setFill(Color.TRANSPARENT);
        greyArc.setStroke(Color.web((locked) ? StyleConstants.LIGHTER_GREY_COLOUR : StyleConstants.GREY_COLOUR));
        greyArc.setStrokeWidth(arcWidth);

        // Red progress arc
        Arc redArc = new Arc();
        redArc.setCenterX(centerX);
        redArc.setCenterY(centerY);
        redArc.setRadiusX(arcRadius);
        redArc.setRadiusY(arcRadius);
        redArc.setLength(-(360 * completion));
        redArc.setType(ArcType.OPEN);
        redArc.setFill(Color.TRANSPARENT);
        redArc.setStroke(Color.web(StyleConstants.RED_COLOUR));
        redArc.setStrokeWidth(arcWidth);
        redArc.setStrokeLineCap(StrokeLineCap.ROUND);

        // Blue circle
        Circle blueCircle = new Circle(centerX, centerY, blueCircleRadius,
                Color.web((locked) ? StyleConstants.LIGHTER_GREY_COLOUR : StyleConstants.BLUE_COLOUR));

        // Add arcs/circle
        pane.getChildren().addAll(greyArc, redArc, blueCircle);

        // Module name label
        Label label = new Label(moduleName);
        label.setFont(antipastoFont); // set the loaded font
        label.setTextFill(Color.web((locked) ? StyleConstants.LIGHTER_GREY_COLOUR : StyleConstants.GREY_COLOUR));
        label.setAlignment(Pos.CENTER);

        // Adjust/truncate if needed
        adjustFontSizeAndTruncate(label, 300, 26, 22);

        // Put the arcs + label into a VBox
        VBox vbox = new VBox(pane, label);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(5);

        // create a StackPane for the final button
        StackPane stackPane = new StackPane(vbox);
        stackPane.setStyle("-fx-cursor: hand;");

        // Make it clickable
        stackPane.setOnMouseClicked(event -> {
            if (onClickAction != null) {
                onClickAction.run();
            }
        });

        return stackPane;
    }

    private static void adjustFontSizeAndTruncate(Label label, double maxWidth, int maxFontSize, int minFontSize) {
        // Start the label at maxFontSize
        Font font = loadSafeFont("/fonts/AntipastoPro.ttf", maxFontSize);
        label.setFont(font);

        // Decrease font size until the text fits or we reach minFontSize
        while (label.getFont().getSize() > minFontSize
                && measureTextWidth(label) > maxWidth) {

            double newSize = label.getFont().getSize() - 1;
            Font smaller = loadSafeFont("/fonts/AntipastoPro.ttf", newSize);
            label.setFont(smaller);
        }

        // If it's still too wide at minFontSize, truncate with "..."
        if (label.getFont().getSize() == minFontSize && measureTextWidth(label) > maxWidth) {
            String text = label.getText();
            while (measureTextWidth(label) > maxWidth && text.length() > 3) {
                text = text.substring(0, text.length() - 1);
                label.setText(text + "...");
            }
        }
    }

    /**
     * Safely loads a font from the given resource path at the specified size.
     * Returns a fallback system font if loading fails.
     */
    private static Font loadSafeFont(String resourcePath, double size) {
        try (InputStream fontStream = ModuleButton.class.getResourceAsStream(resourcePath)) {
            if (fontStream != null) {
                Font loaded = Font.loadFont(fontStream, size);
                if (loaded != null) {
                    return loaded;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // fallback to a system font if the custom font can't load
        return Font.font("System", size);
    }

    /**
     * A helper to measure the current text width in the label,
     * forcing a layout pass so we get an accurate measurement.
     */
    private static double measureTextWidth(Label label) {
        // make sure CSS is applied
        label.applyCss();
        // measure its layout bounds
        return label.getLayoutBounds().getWidth();
    }

}
