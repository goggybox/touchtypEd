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

public class DividerLine {

    /**
     * creates a divider line with text in the middle
     *
     * @param text is the text to display in the middle of the divider line
     * @return an HBox container containing the divider lines and text
     */
    public static HBox createDividerLineWithText(String text) {
        int lineWidth = 5;
        double totalWidth = 530.0; // the total width of the HBox (lines + text + spacing)
        double spacing = 10.0; // the spacing between the lines and text

        if (!text.isEmpty()) {
            // create the text
            Text dividerText = new Text(text);
            dividerText.setFont(Font.font("Antipasto", 32));
            dividerText.setFill(Color.web(StyleConstants.GREY_COLOUR));
            dividerText.setTranslateY(-2);

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


}
