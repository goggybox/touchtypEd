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

/**
 * this class contains the createLessonButton method
 */
public class ModuleButton {

    /**
     * returns a StackPane that represents a module in the Learn page.
     * @param moduleName is the name of the module to display
     * @param completion is the level of completion of the module, represented by the red arc
     * @param onClickAction is the action to perform when the button is clicked
     * @return a StackPane to be displayed
     */
    public static StackPane createModuleButton(String moduleName, double completion, Runnable onClickAction) {

        int arcRadius = 53;
        int arcWidth = 10;
        int blueCircleRadius = 37;

        // create pane; this allows us to manually centre arcs and circle
        Pane pane = new Pane();

        // centre position
        double centerX = arcRadius + arcWidth / 2;
        double centerY = arcRadius + arcWidth / 2;

        // Gray arc (full circle)
        Arc greyArc = new Arc();
        greyArc.setCenterX(centerX);
        greyArc.setCenterY(centerY);
        greyArc.setRadiusX(arcRadius);
        greyArc.setRadiusY(arcRadius);
        greyArc.setLength(360);
        greyArc.setType(ArcType.OPEN);
        greyArc.setFill(Color.TRANSPARENT);
        greyArc.setStroke(Color.web(StyleConstants.GREY_COLOUR));
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
        Circle blueCircle = new Circle(centerX, centerY, blueCircleRadius, Color.web(StyleConstants.BLUE_COLOUR));

        // Add the arcs and circle to the Pane
        pane.getChildren().addAll(greyArc, redArc, blueCircle);

        // Lesson label
        Label label = new Label(moduleName);
        label.setStyle("-fx-font-family: 'Antipasto'; -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #3C3C3C;");

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
}