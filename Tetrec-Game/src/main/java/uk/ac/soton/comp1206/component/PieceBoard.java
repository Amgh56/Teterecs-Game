package uk.ac.soton.comp1206.component;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;


/**
 * <p>PieceBoard class.</p>
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class PieceBoard extends GameBoard {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    private boolean flag;

    /**
     * <p>Constructor for PieceBoard.</p>
     *
     * @param width  a double
     * @param height a double
     * @param flag   a boolean
     */
    public PieceBoard(double width, double height, boolean flag) {
        super(new Grid(3, 3), width, height);
        this.flag = flag;
    }


    /**
     * <p>Constructor for PieceBoard.</p>
     *
     * @param width  a double
     * @param height a double
     */
    public PieceBoard(double width, double height) {
        // Call the parent constructor with a new Grid of 3x3 and the specified dimensions
        super(new Grid(3, 3), width, height);
        // You can initialize other properties or configurations here as needed
        logger.info("PieceBoard created with width: " + width + " and height: " + height);
    }

    /**
     * This method is to display the 3 * 3 grid in the game
     *
     * @param piece the game piece to be displayed
     */
    public void settingPieceToDisplay(GamePiece piece) {
        // First thing clear the previous lines so new blocks can be placed without overlapping
        clearingLines();
        if (piece == null) {
            logger.error("Attempted to display a null piece");
            return; // Do nothing if the piece is null
        }

        // the 2D array representing the game blocks
        int[][] blocks = piece.getBlocks();

        logger.info("Displaying the grid in the center of the grid");

        //Iterate over the blocks of the game
        for (int cols = 0; cols < blocks.length; cols++) {
            for (int rows = 0; rows < blocks[cols].length; rows++) {
                // if the current block is grater than 0 then set it in the grid
                if (blocks[cols][rows] > 0) {
                    grid.set(cols, rows, blocks[cols][rows]);


                }


            }
        }

    }

    /**
     * This method clear the lines of the grid
     */
    public void clearingLines() {
        logger.info("clearing grid");
        // iterate through the blocks and set them to 0
        for (int i = 0; i < grid.getCols(); i++) {
            for (int j = 0; j < grid.getRows(); j++) {
                grid.set(i, j, 0);


            }
        }


    }


}
