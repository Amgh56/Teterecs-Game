package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.RightClickedListener;
import uk.ac.soton.comp1206.scene.ChallengeScene;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class Game implements RightClickedListener {

    /**
     * An instance variable of the interface
     */
    private NextPieceListener nextPieceListener;


    /**
     * an instance variable of the GamePiece class
     */
    protected GamePiece currentPiece;


    /**
     * an instance variable of the GamePiece class
     */
    protected GamePiece nextPiece;

    /**
     * an instance variable of the class GamePiece
     */
    protected GamePiece followingPieces;

    /**
     * this is to track the score
     */
    protected final IntegerProperty highScore = new SimpleIntegerProperty(0);

    /**
     * the score of the current player
     */
    protected final IntegerProperty score = new SimpleIntegerProperty(0);
    /**
     * level of the current game
     */
    protected final IntegerProperty level = new SimpleIntegerProperty(0);
    /**
     * lives of the current player
     */
    protected final IntegerProperty lives = new SimpleIntegerProperty(3);
    /**
     * the multiplayer game
     */
    protected final IntegerProperty multiplier = new SimpleIntegerProperty(1);


    Random random = new Random();

    private static final Logger logger = LogManager.getLogger(Game.class);


    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    private Timer gameTimer;


    /**
     * Initialization of Multimedia class
     */
    private static Multimedia multimedia = new Multimedia();

    /**
     * An instance variable of the lineCleared Listener interface
     */
    private final Set<LineClearedListener> lineClearedListeners = new HashSet<>();

    /**
     * An instance of the GameLoopListener
     */
    private GameLoopListener gameLoopListener;


    private GameWindow gameWindow;


    // This is using an array and the Pair to pair each player with his score
    private final ArrayList<Pair<String, Integer>> playersScoreList = new ArrayList<>();

    // This convert the ArrayList to ObservableList to track changes
    private final ObservableList<Pair<String, Integer>> observableScoreList = FXCollections.observableArrayList(playersScoreList);

    // This what wrap up the observableList in a ListProperty this now can be used to bind things in UI
    private final ListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(observableScoreList);

    // this display things from the localScores simpleProperty anything
    // that change in localScores will also change in listView
    private final ListView<Pair<String, Integer>> listView = new ListView<>();


    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols           number of columns
     * @param rows           number of rows
     * @param challengeScene a {@link uk.ac.soton.comp1206.scene.ChallengeScene} object
     * @param gameWindow     a {@link uk.ac.soton.comp1206.ui.GameWindow} object
     */
    public Game(int cols, int rows, ChallengeScene challengeScene, GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        this.cols = cols;
        this.rows = rows;
        //Create a new grid model to represent the game state
        this.grid = new Grid(cols, rows);

        // Bind the ListView's items property to the listProperty to ensure any changes also reflects in the listView
        listView.itemsProperty().bind(localScores);


    }

    /**
     * <p>Getter for the field <code>score</code>.</p>
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getScore() {
        return score;
    }

    /**
     * <p>Getter for the field <code>level</code>.</p>
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getLevel() {
        return level;
    }

    /**
     * <p>Getter for the field <code>lives</code>.</p>
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getLives() {
        return lives;
    }

    /**
     * <p>Getter for the field <code>multiplier</code>.</p>
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getMultiplier() {
        return multiplier;
    }

    /**
     * <p>Getter for the field <code>highScore</code>.</p>
     *
     * @return a {@link javafx.beans.property.IntegerProperty} object
     */
    public IntegerProperty getHighScore() {
        return highScore;
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        startGameLoop();
    }

    /**
     * assigns GamePiece.createPiece with a random index from 0 to GamePiece. PIECES - 1
     *
     * @return a new game piece
     */
    public GamePiece spawnPiece() {
        return GamePiece.createPiece(random.nextInt(GamePiece.PIECES));
    }


    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        // spawn a new game piece and assign  it to currentPiece variable
        currentPiece = spawnPiece();
        // preload the nextPiece making the
        nextPiece = spawnPiece();
        // this while loop ensures that every board get a distinct element
        while (nextPiece.equals(currentPiece)) {
            nextPiece = followingPieces;
        }
        // preload the followingPiece
        followingPieces = spawnPiece();
        // this while loop ensures that every pieceBoard hava a different random blocks
        while (followingPieces.equals(nextPiece) || followingPieces.equals(currentPiece)) {
            followingPieces = spawnPiece();
        }
        updatePieceBoards();
    }

    /**
     * cycling the pieces
     */
    public void nextPiece() {
        logger.info("moving to a new piece ");
        // here it works like a cycle
        currentPiece = nextPiece; // moving the nextPiece to be the current piece
        nextPiece = followingPieces; // moving the following piece to be the nextPiece
        followingPieces = spawnPiece(); // spawn a new following piece
        updatePieceBoards();
        // to update the piece boards


    }


    /**
     * A method to check if a block can be placed or not and perform actions after a block is blocked
     *
     * @param gameBlock a {@link uk.ac.soton.comp1206.component.GameBlock} object
     */
    public void blockClicked(GameBlock gameBlock) {
        logger.info("Block clicked at position (" + gameBlock.getX() + "," + gameBlock.getY() + ")");
        // Calculate the position and attempt to place the piece
        int blockX = gameBlock.getX();
        int blockY = gameBlock.getY();
        if (grid.canPlayPiece(this.nextPiece, blockX, blockY)) {
            grid.playPiece(nextPiece, blockX, blockY);
            multimedia.playAnAudio("place.wav");
            nextPiece();
            afterPiece();
            resetTimer();
        } else {
            multimedia.playAnAudio("fail.wav");
        }

    }


    /**
     * Identifies and clears fully filled horizontal and vertical lines on the grid.
     * This method performs two main tasks:
     * 1. It iterates through each row to check  and clear fully filled horizontal lines.
     * 2. It iterates through each column to check  and clear fully filled vertical lines.
     * A HashSet is used to monitor block coordinates that need to be cleaned, guaranteeing accurate clearance and the absence of duplicates.
     * This approach handles intersecting lines by only marking each block once for clearing, regardless of its part in horizontal or vertical lines.
     * Detailed logging provides insights into the process, the identification of fully filled lines, and the actions taken to clear them.
     */
    public void afterPiece() {
        // A HashSet was used to store the coordinates of the blocks to be cleared
        HashSet<GameBlockCoordinate> clearingLines = new HashSet<>();

        int totalClearingLines = 0; // A counter to keep tracking the total lines need to be cleared;


        // iterating through the grid from top of the rows tell bottom
        for (int y = 0; y < grid.getRows(); y++) {
            // counter initialized to for the filled blocks in a row
            int filledHorizontalLines = 0;
            // Loop through each column in the current row
            for (int x = 0; x < grid.getCols(); x++) {
                // a condition checks if the index is greater than zero which probably will be 1 if not 0
                if (grid.get(x, y) > 0) {

                    filledHorizontalLines++;// then  filled Lines counter increases by one
                }
            }

            logger.info("Row " + y + ": " + filledHorizontalLines + " filled lines out of " + grid.getCols() + " total Lines.");

            // check if the whole line is full (columns of the row)
            if (filledHorizontalLines == grid.getCols()) {
                // the counter is incrementing
                totalClearingLines++;
                // if so, iterate throw the blocks and add it to the set of clearing
                for (int x = 0; x < grid.getCols(); x++) {

                    clearingLines.add(new GameBlockCoordinate(x, y));// clear the grid
                }
                logger.info("Row " + y + " is full and will be cleared.");


            }

        }
        // iterate through the columns of the grid
        for (int x = 0; x < grid.getCols(); x++) {
            // to track current Vertical filled Lines
            int filledVerticalLines = 0;
            // iterate through each row in the current column
            for (int y = 0; y < grid.getRows(); y++) {
                // if the block is occupied
                if (grid.get(x, y) > 0) {
                    filledVerticalLines++; // increment the filled  vertical lines
                }
            }
            logger.info("Column " + x + ": " + filledVerticalLines + " filled vertical lines out of " + grid.getRows() + " total cells.");
            // check if the current column is fully filled
            if (filledVerticalLines == grid.getRows()) {
                // the counter is incrementing
                totalClearingLines++;
                // then add all blocks to be cleared
                for (int y = 0; y < grid.getRows(); y++) {
                    clearingLines.add(new GameBlockCoordinate(x, y));
                }
            }

        }


        // after clearing all lines iterate through the cleared lines adn set there values to zero
        for (GameBlockCoordinate coordinates : clearingLines) {
            grid.set(coordinates.getX(), coordinates.getY(), 0);
            logger.info("Clearing cells at (" + coordinates.getX() + ", " + coordinates.getY() + ")");
            // notify the listner if there is a  line to clear
            notifyBlocks(clearingLines);

        }


        // check if any of the lines are cleared
        if (totalClearingLines > 0) {
            // updating the score of the game based on the formula in the score method
            score(totalClearingLines, clearingLines.size());

            // incrementing the multiplier
            multiplier.set(multiplier.get() + 1);

        } else {
            // resting the multiplier
            multiplier.set(1);
        }

    }

    /**
     * This method updates the score of the game and upgrade the level depending on the score points
     *
     * @param numberOfLines  number of lines cleared
     * @param numberOfBlocks numberOfBlocks cleared in the lines
     */
    public void score(int numberOfLines, int numberOfBlocks) {
        // A condition to ensure that score won't update unless there are lines cleared
        if (numberOfLines > 0) {
            // The formula for calculating the new score
            int addingScore = numberOfLines * numberOfBlocks * 10 * multiplier.get();
            //updating the score by adding the current score and the new one
            score.set(score.get() + addingScore);


            logger.info("Score updated: " + score.get() + " by adding " + addingScore + " points.");

            // for every 1000 point the level is upgrading score
            int upgradeLevel = score.get() / 1000;

            // this condition checks if the upgradeLevel is not in the same level if not then it sets the level to the new level
            if (upgradeLevel != level.get()) {
                // setting it to the new level
                level.set(level.get() + upgradeLevel);
                logger.info("The Current Level after updating is: " + level.get());

            }

        }


    }

    /**
     * set the listeners for the updated pieces
     *
     * @param nextPieceListener a {@link uk.ac.soton.comp1206.event.NextPieceListener} object
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
        logger.info("NextPieceListener set successfully");
    }

    /**
     * Update the pieceBoard UI components on the current held pieces
     */
    private void updatePieceBoards() {

        if (nextPieceListener == null) {
            logger.error("nextPieceListener is null at this point");
        } else {
            nextPieceListener.nextPiece(nextPiece, followingPieces);
        }


    }

    /**
     * adds a listner for the clearance events
     * when the lines are cleared the lines get notified
     *
     * @param lineClearedListener this is the linter that reacts to the
     */
    public void addLinesCleared(LineClearedListener lineClearedListener) {
        // adds the listner to the set of listeners that will be notified
        lineClearedListeners.add(lineClearedListener);
        logger.info("LineClearedListener added: {}", lineClearedListener);
    }


    /**
     * Notifies all listeners registered that the block is cleared
     *
     * @param clearedBlocks clearedBlocks The set of coordinates for the blocks that have been cleared.
     */
    public void notifyBlocks(Set<GameBlockCoordinate> clearedBlocks) {
        // iterate through all the registered listeners
        for (LineClearedListener listner : this.lineClearedListeners) {
            // notifying all listeners by passing the cleared blocks
            listner.lineCleared(clearedBlocks);

        }
    }


    /**
     * A method for rotating the game piece
     */
    public void rotateNextPiece() {

        // this condition rotate the current piece if it is not null
        if (nextPiece != null) {
            nextPiece.rotate();
            multimedia.playAnAudio("rotate.wav");
            logger.info("next piece rotate successfully");
            updatePieceBoards();
        }

        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(nextPiece, followingPieces);


        }
    }

    /**
     * A method to rotate the piece clockwise
     */
    public void rotateClockWise() {
        multimedia.playAnAudio("rotate.wav");
        // indicating how many  rotations should it be as 90 degree by setting it to three
        nextPiece.rotate(3);
        // updating the PieceBoards
        updatePieceBoards();

        logger.info("Rotating Clockwise");


    }

    /**
     * A method to rotate the piece clockwise
     */
    public void rotateOnClock() {
        multimedia.playAnAudio("rotate.wav");
        // indicating how many  rotations should it be as 90 degree by setting it to three
        nextPiece.rotate(1);
        // updating the PieceBoards
        updatePieceBoards();


    }


    /**
     * {@inheritDoc}
     * <p>
     * this is a method implemented from the RightClickedListner interface
     */
    @Override
    public void rightClick(int x, int y) {
        rotateClockWise();

    }

    /**
     * A method to swap the currentPiece
     */
    public void swapCurrentPiece() {
        GamePiece temporarily = this.nextPiece;
        this.nextPiece = this.followingPieces;
        this.followingPieces = temporarily;
        multimedia.playAnAudio("transition.wav");

        updatePieceBoards();


    }

    /**
     * Calculates the delay for the game loop timer based on the current game level.
     * The delay decreases as the level increases*
     *
     * @return The calculated delay in milliseconds for the game loop timer.
     */
    public int getTimerDelay() {
        // Calculate the delay based on the game level. The base delay is 1200 milliseconds.
        // For each level, reduce the delay by 500 milliseconds to increase the game's difficulty.
        int delay = 12000 - (500 * this.getLevel().get());

        // ensure that the delay doesn't go below 2500 so if the delay is less we take the 2500 milliseconds
        delay = Math.max(2500, delay);
        // return the delay in milliseconds
        return delay;
    }

    /**
     * Starts the game loop using a Timer to schedule repeated execution of game logic.
     * This loop runs the method at fixed intervals from the getTimerDelay method .
     */
    public void startGameLoop() {
        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                gameLoop();
            }
        }, getTimerDelay());

        if (gameLoopListener != null) {
            gameLoopListener.onGameLoop();
        }
    }


    /**
     * Executes a cycle of game logic, updating game states like lives and initiating game transitions.
     * This method handles decrementing lives if block not placed or timer finished, moves to the next game phase, ends the game if lives run out,
     * plays a sound effect for life loss, and resets the game timer.
     */
    public void gameLoop() {
        Platform.runLater(() -> {
            logger.info("Lives: " + lives.get());

            // Only decrement lives if no block has been placed and lives are greater than zero
            if (lives.get() > 0) {
                lives.set(lives.get() - 1);
                logger.info("Decrementing lives to " + lives.get() + " and resetting multiplier.");
                multiplier.set(1);
                nextPiece();


            } else {
                // else go to the scoresScene means that the game ended
                endGameScene();
                return;
            }
            multimedia.playAnAudio("lifelose.wav");
            resetTimer();
        });
    }


    /**
     * stops the gameLoop by shutting down the scheduler
     */
    public void stopGameLoop() {
        //check if the gimeTimer is not null
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
        if (gameLoopListener != null) {
            gameLoopListener.endGameLoop();

        }
    }


    /**
     * Resets the gameLoop timer by canceling the current task and rescheduling it
     */
    public void resetTimer() {
        logger.info("reseting the timer");
        stopGameLoop();
        startGameLoop(); // Restart the game loop with the updated delay
    }


    /**
     * A method to link the registered listeners to the gameLoopListner
     *
     * @param gameLoopListener A listner
     */
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }


    /**
     * This method transit to the score scene
     */
    public void endGameScene() {
        if (gameWindow != null) {
            logger.info("going to score menu");
            stopGameLoop();
            multimedia.backGroundMusicStop();
            this.gameWindow.startScoreScene(this);
            multimedia.playAnBackgroundMusic("end.wav");
            logger.info("To Scores Scene");

        }
    }

    /**
     * this method so once esc is pressed go back to the menu
     */
    public void goBackToMenu() {
        if (gameWindow != null) {
            logger.info("going to score menu");

            stopGameLoop();
            multimedia.backGroundMusicStop();
            this.gameWindow.startMenu();

        }
    }

    /**
     * Get the grid model inside this representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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


    /**
     * <p>Getter for the field <code>nextPiece</code>.</p>
     *
     * @return a {@link uk.ac.soton.comp1206.game.GamePiece} object
     */
    public GamePiece getNextPiece() {
        return nextPiece;
    }

    /**
     * <p>Getter for the field <code>followingPieces</code>.</p>
     *
     * @return a {@link uk.ac.soton.comp1206.game.GamePiece} object
     */
    public GamePiece getFollowingPieces() {
        return followingPieces;

    }
}
