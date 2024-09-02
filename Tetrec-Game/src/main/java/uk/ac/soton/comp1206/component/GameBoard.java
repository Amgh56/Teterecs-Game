package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

import java.util.HashSet;
import java.util.Set;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class GameBoard extends GridPane {

    /**
     * An instance of the Game class
     */
    protected Game game;
    /**
     * An instance of the RightClickedListner
     */
    protected RightClickedListener rightClickedListener;

    /**
     * An instance of the GameBoard class
     */

    protected GameBlock gameBlock;
    protected GameBlock myHover;


    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;

    /**
     * An instance variable of the lineCleared Listener interface
     */
    private final Set<LineClearedListener> lineClearedListeners = new HashSet<>();


    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     *
     * @param grid   linked grid
     * @param width  the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        //Build the GameBoard
        build();
    }


    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     *
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}", cols, rows);
        blocks = new GameBlock[cols][rows];


        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for (var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x, y);


            }
        }
        // adding the right click listner to the build


    }

    /**
     * Create a block at the given x and y position in the GameBoard
     *
     * @param x column
     * @param y row
     * @return a {@link uk.ac.soton.comp1206.component.GameBlock} object
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block, x, y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x, y));


        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));
        block.setOnMouseEntered(
                (e) -> {
                    this.myHover(block);
                });
        block.setOnMouseExited(
                (e) -> {
                    this.unhoveringthePieceBoards(block);
                });


        return block;


    }


    /**
     * Set the listener to handle an event when a block is clicked
     *
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Set the listner to handle an event when right click is occurred
     *
     * @param listener listner to add
     */
    public void setRightClickedListener(RightClickedListener listener) {
        this.rightClickedListener = listener;


    }


    /**
     * Starts fade-out animations for blocks given by their grid positions. It goes through each position,
     * finds the matching block on the board, and makes it start fading. This method visually represents the removal
     * of blocks from the game.
     *
     * @param blocksToFade Positions of the blocks to fade out.
     */
    public void fadeOut(Set<GameBlockCoordinate> blocksToFade) {
        logger.info("Fading out {} blocks", blocksToFade.size());
        // loop through the set of coordinates to fade out every single block
        for (GameBlockCoordinate coordinates : blocksToFade) {
            // get the blocks at this coordinates
            GameBlock block = getBlock(coordinates.getX(), coordinates.getY());
            // if block is not null start the fade out animation
            if (block != null) {
                block.fadeOut();
                logger.info("Fading out block at ({}, {})", coordinates.getX(), coordinates.getY());

            }
        }
    }


    /**
     * Triggered when a block is clicked. Call the attached listener.
     *
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        if (event.getButton() == MouseButton.PRIMARY) {
            logger.info("left block clicked {}", block);
            if (blockClickedListener != null) {
                blockClickedListener.blockClicked(block);
            }
        }
        if (event.getButton() == MouseButton.SECONDARY) {
            logger.info("right block clicked {}", block);
            if (rightClickedListener != null) {
                rightClickedListener.rightClick(block.getX(), block.getY());


            }

        }
    }


    /**
     * Handles hover state for a game block if the current object isn't a {@code PieceBoard}.
     * It unsets any previously hovered block and sets the new one.
     *
     * @param gameBlock the game block to hover.
     */
    public void myHover(GameBlock gameBlock) {
        if (!(this instanceof PieceBoard)) {
            //refreshes the hover
            if (this.myHover != null) {
                this.unhoveringthePieceBoards(this.myHover);
            }
            this.myHover = gameBlock;
            gameBlock.setHovered(true);
        }
    }

    /**
     * let the set the unHoverdPiece to false
     *
     * @param hover a {@link uk.ac.soton.comp1206.component.GameBlock} object
     */
    public void unhoveringthePieceBoards(GameBlock hover) {
        hover.setHovered(false);
    }
}



