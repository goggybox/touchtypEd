package com.example.touchtyped.model;

import com.example.touchtyped.constants.StyleConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;

/**
 * this class contains the createLessonButton method
 */
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

        // create pane; this allows us to manually centre arcs and circle
        Pane pane = new Pane();
        pane.setPrefWidth(250);

        Font antipastoFont = Font.loadFont(ModuleButton.class.getResource("/fonts/AntipastoPro.ttf").toExternalForm(), 26);

        // centre position
        double centerX = pane.getPrefWidth() / 2;
        double centerY = arcRadius + arcWidth / 2;

        // is module locked?
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

        // Red progress arc (half circle)
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
        redArc.setStrokeLineCap(StrokeLineCap.ROUND); // rounded edge

        // Blue circle background
        Circle blueCircle = new Circle(centerX, centerY, blueCircleRadius, Color.web((locked) ? StyleConstants.LIGHTER_GREY_COLOUR : StyleConstants.BLUE_COLOUR));

        // Add the arcs and circle to the Pane
        pane.getChildren().addAll(greyArc, redArc, blueCircle);

        // Lesson label
        Label label = new Label(moduleName);
        label.setFont(antipastoFont);
        label.setTextFill(Color.web((locked) ? StyleConstants.LIGHTER_GREY_COLOUR : StyleConstants.GREY_COLOUR));

        // adjust font size and truncate if needed
        adjustFontSizeAndTruncate(label, 300, 26, 22);
        label.setAlignment(Pos.CENTER);

        // Wrap in VBox to include the label
        VBox vbox = new VBox(pane, label);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(5);

        // create StackPane
        StackPane stackPane = new StackPane(vbox);

        // make it clickable
        stackPane.setOnMouseClicked(event -> {
            if (onClickAction != null) {
                onClickAction.run();
            }
        });

        // set cursor to be a hand
        stackPane.setStyle("-fx-cursor: hand;");

        // Return the final StackPane
        return stackPane;
    }

    private static void adjustFontSizeAndTruncate(Label label, double maxWidth, int maxFontSize, int minFontSize) {
        String text = label.getText();
        Font font = Font.loadFont(ModuleButton.class.getResource("/fonts/AntipastoPro.ttf").toExternalForm(), maxFontSize);

        // start with the maximum font size, and decrease until text fits or the min font size is reached
        while (font.getSize() > minFontSize && label.getFont().getSize() * text.length() > maxWidth) {
            font = Font.loadFont(ModuleButton.class.getResource("/fonts/AntipastoPro.ttf").toExternalForm(), font.getSize() - 1);
            label.setFont(font);
        }

        // if the text still doesn't fit, truncate it
        if (label.getFont().getSize() == minFontSize && label.getWidth() > maxWidth) {
            while (label.getWidth() > maxWidth && text.length() > 3) {
                text = text.substring(0, text.length() - 3);
                label.setText(text + "...");
            }
        }

    }
}