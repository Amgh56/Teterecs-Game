package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 * <p>
 * The Grid should be linked to a GameBoard for it's display.
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class Grid {

    /**
     * logger to help me track my code and use it to debug
     */
    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;


    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     *
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * to check if a given game place can be placed at a specific position on the grid
     *
     * @param gamePiece the game piece to be played, represented as a 3x3 int array where non-zero values indicate the presence of a block
     * @param x         x-coordinates  represents  column index
     * @param y         The y-coordinate represents row index
     * @return true whether their place to play or false whether no place to play
     */
    public boolean canPlayPiece(GamePiece gamePiece, int x, int y) {
        int[][] blocks = gamePiece.getBlocks(); // 3 * 3 array representing the game blocks
        x -= 1; // assign x to the center
        y -= 1; // assign y to the center
        logger.info("Checking if a piece can placed or not " + x + "," + y);
        for (int blockX = 0; blockX < blocks.length; blockX++) { // iterate through the grid columns
            for (int blockY = 0; blockY < blocks[blockX].length; blockY++) {// iterate through the rows of the grid
                if (blocks[blockX][blockY] == 0)
                    continue;
                int gridX = x + blockX;
                int gridY = y + blockY;
                // this condition is to make sure that the block is not exceeding the grid and that it is not negative
                // and also check if the block is already occupied
                if (gridX < 0 || gridX >= this.getCols() || gridY < 0 || gridY >= this.getRows() || this.get(gridX, gridY) != 0) {
                    logger.info("Block at (" + gridX + "," + gridY + ") cannot be placed.");
                    return false;
                }
            }
        }

        logger.info("There is place for the block to be placed at (" + x + "," + y + ")");
        return true;// there is a place to for the block
    }

    /**
     * This method first checks if the piece can be legally placed at the given coordinates without overlapping existing pieces.
     * If the placement is possible, it updates the grid with the game piece's values.
     * The piece is positioned based on its center, requiring an adjustment to the provided coordinates.
     *
     * @param gamePiece game piece to be placed in the grid
     * @param x         x-coordinates columns where the piece should place
     * @param y         y-coordinates rows where the piece should place
     * @return at firs there is a condition that checks if the piece can be placed or not if not it immediately return false,
     * and if there is place then it return true and sets the piece in the grid
     */
    public boolean playPiece(GamePiece gamePiece, int x, int y) {


        // this method checks if a piece can be placed
        if (canPlayPiece(gamePiece, x, y)) {
            x -= 1; // assigning the coordinates  to the center of the grid
            y -= 1;
            int[][] blocks = gamePiece.getBlocks();// 3 * 3 array of int blocks
            for (int blockX = 0; blockX < blocks.length; blockX++) {// loop through each row of the block in the game
                // within each row loop through each [blockX] -> column of the row
                for (int blockY = 0; blockY < blocks[blockX].length; blockY++) {
                    // this get the track coordinates in the loop see if it is occupied or not
                    if (blocks[blockX][blockY] != 0) {
                        set(x + blockX, y + blockY, blocks[blockX][blockY]);

                    }
                }
            }
        }

        logger.info("Piece successfully placed ");
        return true;// true if the piece can be placed and played

    }


    /**
     * Update the value at the given x and y index within the grid
     *
     * @param x     column
     * @param y     row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     *
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

}
