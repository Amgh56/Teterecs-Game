package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * A boolean property hovered
     */
    private final BooleanProperty hovered = new SimpleBooleanProperty(false);


    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);


    /**
     * Create a new single Game Block
     *
     * @param gameBoard the board this block belongs to
     * @param x         the column the block exists in
     * @param y         the row the block exists in
     * @param width     the width of the canvas to render
     * @param height    the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
        hovered.addListener((obs, oldVal, newVal) -> paint());


    }


    /**
     * When the value of this block is updated,
     *
     * @param observable what was updated
     * @param oldValue   the old value
     * @param newValue   the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if (value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
        // check if the game board belong to the piece board not the main grid so if i
        // t is then place the circle in the middle of the block
        if (this.gameBoard instanceof PieceBoard && isMiddleBlock()) {
            paintIndicator();

        }


    }


    /**
     * /**
     * * This method make sure that the block will be placed in the middle of the peace board
     *
     * @return a boolean
     */
    public boolean isMiddleBlock() {
        return this.x == 1 && this.y == 1;
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        //Fill
        // gc.setFill(Color.WHITE);

        // this is rgba colour to achieve the light colour
        gc.setFill(new Color(0.2, 0.2, 0.2, 0.2));

        gc.fillRect(0, 0, width, height);

        hover();
        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Paint this canvas with the given colour
     *
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0, 0, width, height);
        // invoking the method to hover over the block above the painted buttons
        hover();


        //Border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeRect(0, 0, width, height);


    }


    /**
     * A method for drawing the circle in the middle of the pieceBoard
     */
    public void paintIndicator() {
        var gc = getGraphicsContext2D();
        // making a transparent colour of light white
        gc.setFill(new Color(1, 1, 1, 0.5));
        // Draw the circle indicator at the center of the block
        // Calculate the radius of the circle as a function of the block size
        double radius = Math.min(width, height) / 3; // For a circle with a diameter of half the block size

        // Draw the circle in the center of the block
        gc.fillOval((width / 2) - radius, (height / 2) - radius, radius * 2, radius * 2);


    }

    /**
     * A method to highlight the currently hovered block
     */
    public void hover() {
        var gc = getGraphicsContext2D();
        if (hovered.get()) {
            // making a transparent colour of light white
            gc.setFill(new Color(1, 1, 1, 0.5));
            // Fill a rectangle over the entire block
            gc.fillRect(0, 0, width, height);
        }
    }

    /**
     * Starts an animation that makes the block flash green and then fade away. This uses an AnimationTimer to keep track
     * of how long the animation has been running. There are two main parts to the animation:
     * <p>
     * 1. Flash Phase: For a short time, the block turns green. This shows the player that the block is about to disappear.
     * 2. Fade Phase: After flashing, the block slowly becomes transparent over a few seconds, and then it's gone.
     * <p>
     * The animation stops once the block is completely faded out.
     */
    public void fadeOut() {
        logger.info("Starting fadeOut for block at ({}, {})", x, y);
        final long startNanoTimer = System.nanoTime();
        final AnimationTimer fadeTimer = new AnimationTimer() {
            // This instance for the green flash to duration.
            final double flashDuration = 0.2;
            // An instance for the fade duration
            final double fadeDuration = 2.0;

            @Override
            public void handle(long currentNanoTimer) {
                // here we are calculating the time passed since the animation started
                // the difference between the
                // currentTime and the startTime of the animation and converting it from
                // nanoseconds to seconds by dividing it with / 1_000_000_000.0

                double elapsedTime = (currentNanoTimer - startNanoTimer) / 1_000_000_000.0;

                // this checks if the elapsedTime is smaller or equal than the flash duration
                // if it is then the frames will colour green and
                if (elapsedTime <= flashDuration) {
                    //Colouring the blocks with Green
                    paintColor(Color.GREEN.brighter());
                    // here we are making sure that the blocks fade out after turning green
                } else if (elapsedTime > flashDuration && elapsedTime <= fadeDuration) {
                    // this makes the green as time progress the colour becomes transparent
                    double opacity = 1 - ((elapsedTime - flashDuration) / (fadeDuration - flashDuration));
                    // Apply the calculated opacity to the block color, keeping the color green but
                    // making it more transparent as time progresses.
                    paintColor(Color.color(0, 1, 0, opacity));
                } else {
                    // once the elapsed time exceed the flash duration the animation stops and
                    // the blocks becomes empty visually
                    stop();
                    paintEmpty();
                }
            }
        };
        // this starts the animation
        fadeTimer.start();
    }

    /**
     * Get the column of this block
     *
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     *
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     *
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     *
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }


    /**
     * <p>Setter for the field <code>hovered</code>.</p>
     *
     * @param hovered a boolean
     */
    public void setHovered(boolean hovered) {
        this.hovered.set(hovered);
        paint();
    }
}
