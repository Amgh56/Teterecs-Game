package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static javafx.scene.control.skin.TextInputControlSkin.Direction.*;


/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class ChallengeScene extends BaseScene implements GameLoopListener {

    // Listener to handle the event when the next game piece is ready
    protected NextPieceListener nextPieceListener;

    // Board to display the next piece in the game
    protected PieceBoard nextPieceBoard;
    // Board to display the piece that follows the next piece
    protected PieceBoard followingPieceBoard;

    // Logger for logging information, warnings, and errors
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    // Reference to the game logic
    protected Game game;
    // Singleton instance of Multimedia class used for handling multimedia content
    private static Multimedia multimedia = new Multimedia();


    public GameBlock gameBlock;


    // Visual element to represent a timer or progress
    private Rectangle timeBar;

    // Controls animations or sequences of actions over time, e.g., for updating timeBar
    private Timeline timeline;

    // Property to hold and observe changes in high score values
    private final IntegerProperty highScores = new SimpleIntegerProperty();


    private IntegerProperty aimX = new SimpleIntegerProperty(0);
    private IntegerProperty aimY = new SimpleIntegerProperty(0);
    private GameBoard board;


    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");

        setupGame();

        setUpNextPieceListener();

        highScores.set(getHighScore("save.txt"));
        updateHighScores();


    }


    /**
     * sets up a listener so when changes occur to the current and next game piece, it updates the PieceBoard accordingly
     */
    private void setUpNextPieceListener() {
        // this will handle notifications about the new current and next game pieces
        nextPieceListener = (nextPiece, followingPieces) -> {
            logger.info("new current and new next received successfully ");

            // this schedule the update to the UI components
            Platform.runLater(() -> {
                logger.debug("Updating PieceBoards");

                // Update the PieceBoard with the new next piece
                if (nextPieceBoard != null) {
                    nextPieceBoard.settingPieceToDisplay(nextPiece);
                    logger.info("Next PieceBoard updated.");
                }
                // Update the PieceBoard with the new following piece
                if (followingPieceBoard != null) {
                    followingPieceBoard.settingPieceToDisplay(followingPieces);
                    logger.info("Next PieceBoard Debug");
                }


            });
        };
        // Register the listener with the game instance to ensure it receives updates about game pieces.
        this.game.setNextPieceListener(nextPieceListener);

        // Log the successful registration of the listener with the game.
        logger.info("NextPieceListener successfully registered with game.");
    }


    /**
     * {@inheritDoc}
     * <p>
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        // Initialize game settings or components specific to this scene.
        setupGame();

        // Create a new label to indicate the area where upcoming game pieces are shown.
        Label boardsLabel = new Label("Incoming");
        boardsLabel.getStyleClass().add("myscore");
        boardsLabel.setTranslateY(-60);
        boardsLabel.setTranslateX(-35);

        // assigning the nextPieceBoard with width and height
        this.nextPieceBoard = new PieceBoard(120, 120);
        // moving the board toward the left 30 degree
        nextPieceBoard.setTranslateX(-30);
        nextPieceBoard.setTranslateY(190);
        // Add an event handler to nextPieceBoard to handle mouse clicks.
        nextPieceBoard.setOnMouseClicked(event -> {
            // Check if the mouse button pressed is the primary button.
            if (event.getButton() == MouseButton.PRIMARY) {
                // Rotate the next piece in the game logic when the board is clicked.
                game.rotateNextPiece();
                logger.info("Rotating the block");
            }
        });

        // Initialize followingPieceBoard with specific dimensions, smaller than nextPieceBoard
        this.followingPieceBoard = new PieceBoard(70, 70);
        // Set the vertical position of followingPieceBoard to be 200 pixels down from its default position.
        followingPieceBoard.setTranslateY(200);
        // Add a mouse click event handler to the followingPieceBoard.
        followingPieceBoard.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                game.swapCurrentPiece();
                if (nextPieceBoard != null && followingPieceBoard != null) {
                    nextPieceBoard.settingPieceToDisplay(this.game.getNextPiece());
                    followingPieceBoard.settingPieceToDisplay(this.game.getFollowingPieces());
                    logger.info("Pieces are swapping successfully");

                }
            }
        });


        // initializing a vertical box
        VBox vBox = new VBox(10);
        //adding the piece to the Vbox
        vBox.getChildren().addAll(nextPieceBoard, followingPieceBoard, boardsLabel);
        boardsLabel.setAlignment(Pos.TOP_RIGHT);
        nextPieceBoard.setAlignment(Pos.TOP_RIGHT);
        followingPieceBoard.setAlignment(Pos.TOP_RIGHT);


        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());


        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);
        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        // Inside the initialise method of ChallengeScene
        // Ensure gameBoard is properly initialized before this point
        // assinging the fadeout method within the gameBoard instance
        game.addLinesCleared(board::fadeOut);
        board.setRightClickedListener(game);
        mainPane.setCenter(board);


        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        // Creates an HBox with spacing for layout
        HBox hBox = new HBox(10);
        // Centers the HBox contents
        hBox.setAlignment(Pos.TOP_CENTER);
        // Adds some padding
        hBox.setPadding(new Insets(15, 0, 15, 0));

        /**
         * Styling the score
         */
        Label scoreLabel = new Label("Score: ");// initialize label with score
        // this label is to display the value of the score
        Label scoreValue = new Label();
        //  anything will happen to the score will be updated because of the binding
        scoreValue.textProperty().bind(game.getScore().asString());

        // This is for the css styling
        scoreLabel.getStyleClass().add("score");
        scoreValue.getStyleClass().add("score");

        /**
         * Styling the Level
         */
        Label levelLabel = new Label("Level: ");// initialize label with Level
        // this label is to display the current Level
        Label levelValue = new Label();
        // this binding will always update the level
        levelValue.textProperty().bind(game.getLevel().asString());

        // for styling the css of the game
        levelLabel.getStyleClass().add("level");
        levelValue.getStyleClass().add("level");

        /**
         * Styling the lives
         */
        Label livesLabel = new Label("Lives: "); // initialize label with Lives
        // this label is to display the  current lives left
        Label livesValue = new Label();
        //this will always update the lives
        livesValue.textProperty().bind(game.getLives().asString());


        //for styling the labels
        livesLabel.getStyleClass().add("lives");
        livesValue.getStyleClass().add("lives");


        /**
         * Styling the Multiplier
         */
        Label multiplierLabel = new Label("Multiplier: "); // initialize label with Lives
        // this label is to display the current multiplier
        Label multiplierValue = new Label();
        // will always update the multiplayer
        multiplierValue.textProperty().bind(game.getMultiplier().asString());

        //for styling the labels
        multiplierLabel.getStyleClass().add("multiplier");
        multiplierValue.getStyleClass().add("multiplier");


        //creates a new instance of label for the highScores
        Label highScoreLabel = new Label();
        // this bing is automatically appear in the ui once the high score is achieved
        highScoreLabel.textProperty().bind(highScores.asString("HISCORE: %d"));


        // adding the highScoreLabel to the root
        highScoreLabel.getStyleClass().add("hiscore");
        highScoreLabel.setAlignment(Pos.TOP_LEFT);

        File file = new File("save.txt");


        // Adding to HBox
        hBox.getChildren().addAll(scoreLabel, scoreValue, levelLabel, levelValue, livesLabel, livesValue, multiplierLabel, multiplierValue, highScoreLabel);

        timeBar = new Rectangle();
        HBox hBox1 = new HBox(10);
        hBox1.getChildren().add(timeBar);
        hBox1.getStyleClass().add("info");
        hBox1.setAlignment(Pos.BOTTOM_CENTER);

        mainPane.setBottom(hBox1);

        //  position the labels at the top
        mainPane.setTop(hBox);

        // position the vbox to the right of the pane
        mainPane.setRight(vBox);
    }


    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5, this, gameWindow);
        setUpNextPieceListener();
    }


    /**
     * {@inheritDoc}
     * <p>
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.setOnGameLoop(this);
        game.start();
        multimedia.playAnBackgroundMusic("game_start.wav");
        setUpNextPieceListener();
        getHighScore("save.txt");

        nextPieceBoard.settingPieceToDisplay(game.getNextPiece());
        followingPieceBoard.settingPieceToDisplay(game.getFollowingPieces());
        getScene().setOnKeyPressed(this::keyHandler);

        // an audio add to the game
        multimedia.playAnAudio("transition.wav");


        // Set the initial high score
        highScores.set(getHighScore("save.txt"));
        game.getScore().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() > highScores.get()) {
                highScores.set(newVal.intValue());
                saveHighScores("save.txt", newVal.intValue());
            }
        });


    }


    /**
     * This method is to handle the keyEvents
     *
     * @param keyEvent key events like going up going down etc..
     */
    public void keyHandler(KeyEvent keyEvent) {
        KeyCode code = keyEvent.getCode();
        switch (code) {
            case UP:
            case W:
                aimUp();
                break;
            case DOWN:
            case S:
                aimDown();
            case LEFT:
            case A:
                aimLeft();
                break;
            case RIGHT:
            case D:
                aimRight();
                break;
            case ENTER:
            case X:
                blockClicked(board.getBlock(aimX.get(), aimY.get()));
                break;
            case C:
            case E:
            case CLOSE_BRACKET:
                game.rotateClockWise();
                break;
            case Q:
            case Z:
            case OPEN_BRACKET:
                game.rotateOnClock();
                break;
            case SPACE:
            case R:
                game.swapCurrentPiece();
                break;
            case ESCAPE:
                game.goBackToMenu();
                break;
        }
        hoverUpdating();
    }


    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */
    public void blockClicked(GameBlock gameBlock) {
        this.gameBlock = gameBlock;
        game.blockClicked(gameBlock);
    }


    /**
     * setting the aimUp
     */
    public void aimUp() {
        if (aimY.get() > 0) {
            aimY.set(aimY.get() - 1);
        }
    }

    /**
     * setting the aimDown
     */
    public void aimDown() {
        logger.info("Current aimY: {}", aimY.get());
        if (aimY.getValue() < game.getRows() - 1) {
            aimY.set(aimY.getValue() + 1);
            logger.info("Updated aimY: {}", aimY.get());
        }
    }


    /**
     * setting the aimLeft
     */
    protected void aimLeft() {
        if (aimX.get() > 0) {
            aimX.set(aimX.get() - 1);
        }
    }

    /**
     * setting the aimRight
     */
    protected void aimRight() {
        if (aimX.get() < game.getCols() - 1) {
            aimX.set(aimX.get() + 1);
        }
    }

    /**
     * Retrieves the highest score from a specified file.
     *
     * @param filePath The path to the file containing score records in the format "Name:Score".
     * @return The highest score as an integer, or 0 if the file doesn't exist or is unreadable.
     */
    private int getHighScore(String filePath) {
        Path path = Paths.get(filePath);
        int highestScore = 0;
        try {
            if (!Files.exists(path)) {
                // If no file exists, return 0
                return 0;
            }
            List<String> allLines = Files.readAllLines(path);
            for (String line : allLines) {
                String[] nameScore = line.split(":");
                if (nameScore.length == 2) {
                    try {
                        int score = Integer.parseInt(nameScore[1].trim());
                        if (score > highestScore) {
                            highestScore = score;
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Failed to parse score: " + nameScore[1], e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read the high scores file.", e);
        }
        return highestScore;
    }


    /**
     * Updates the high scores file by adding a new score if it is higher than any existing scores.
     *
     * @param filePath The path to the file where high scores are stored. Each line should be in the format "Name:Score".
     * @param newScore The new score to potentially add to the file.
     */
    public void saveHighScores(String filePath, int newScore) {
        File file = new File(filePath);
        Map<String, Integer> scores = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    int score = Integer.parseInt(parts[1].trim());
                    scores.put(parts[0], score);

                }
            }
        } catch (IOException e) {
            logger.error("Error reading scores", e);
        }


    }


    /**
     * sets up listeners on the game score to update the score automatically
     */
    public void updateHighScores() {
        //adding listeners to the score property
        game.getScore().addListener((obs, oldval, newVal) -> {
            //check if the new score is greater than highScore
            if (newVal.intValue() > highScores.getValue()) {
                //update the highScore property
                highScores.set(newVal.intValue());
                //Save the new highScore
                saveHighScores("save.txt", newVal.intValue());
            }

        });
    }


    /**
     * {@inheritDoc}
     * <p>
     * Initializes and starts the game loop animation. It configures a timeline that animates the timeBar's width to 0
     * and changes its color from green to red over the specified duration of the game's timer delay.
     */
    @Override
    public void onGameLoop() {
        timeline = new Timeline();
        double duration = game.getTimerDelay();

        timeBar.setWidth(gameWindow.getWidth() - 50);
        timeBar.setHeight(20);
        timeBar.setFill(Color.GREEN);
        KeyFrame endFrame = new KeyFrame(
                javafx.util.Duration.millis(duration),
                new KeyValue(timeBar.widthProperty(), 0), // Animate width to 0
                new KeyValue(timeBar.fillProperty(), Color.RED) // Change color to red
        );
        timeline.getKeyFrames().add(endFrame);
        timeline.playFromStart();
    }


    /**
     * {@inheritDoc}
     * <p>
     * Resets the visual properties of the timeBar at the end of the game loop, setting its width and height.
     */
    @Override
    public void endGameLoop() {
        timeBar.setWidth(gameWindow.getWidth() - 50);
        timeBar.setHeight(20);
    }

    /**
     * Updates the hover state of the game block located at the current aim coordinates (aimX, aimY).
     */
    public void hoverUpdating() {
        board.myHover(board.getBlock(aimX.get(), aimY.get()));
    }
}
