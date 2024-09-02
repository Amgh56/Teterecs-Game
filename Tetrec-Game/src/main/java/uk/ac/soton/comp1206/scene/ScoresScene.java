package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;


/**
 * <p>ScoresScene class.</p>
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class ScoresScene extends BaseScene implements CommunicationsListener {

    private Game game;

    private ListProperty<Pair<String, Integer>> localScores;

    private final static Logger logger = LogManager.getLogger();

    private ScoresList scoresList;
    ArrayList<Pair<String, Integer>> siu;

    private SimpleListProperty<Pair<String, Integer>> remoteScores;

    public Communicator communicator;

    private ScoresList localScoresList;
    private ScoresList onlineScore;


    /**
     * This Scene represents the score scene the remote and the local scores
     *
     * @param gameWindow the main window where this game is displayed
     * @param game       the current game
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game = game;
        // initialize local scores with an empty observable list.
        this.localScores = new SimpleListProperty<>(loadScores("save.txt"));
        this.communicator = gameWindow.getCommunicator();
        //Registering this scene as a listener to the communicator
        this.communicator.addListener(this);
        siu = new ArrayList<>();
        remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList());
        build();


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialise() {

        if (communicator != null) {
            communicator.addListener(message -> Platform.runLater(() -> receiveCommunication(message)));
            logger.info("Communicator listener added.");
        } else {
            logger.error("Communicator has not been initialized.");
        }

        loadOnlineScores();
        this.scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                exitPressed();

            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {

        // create a new GamePane as the root container for UI elements
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        var mainPane = new BorderPane();
        mainPane.setMaxHeight(gameWindow.getHeight());
        mainPane.setMaxWidth(gameWindow.getWidth());
        root.getChildren().add(mainPane);
        mainPane.getStyleClass().add("menu-background");

        HBox hBox = new HBox();
        // load an image from a url folder by using getResource and the path name to a string representation by using toExternalForm
        Image image = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
        //This creates an Image view
        ImageView imageView = new ImageView(image);
        hBox.getChildren().add(imageView);
        // This sets the Height of the images
        imageView.setFitHeight(100);
        //This sets the Width of the images
        imageView.setFitWidth(600);

        mainPane.setTop(hBox);
        BorderPane.setAlignment(imageView, Pos.CENTER);
        mainPane.setTop(imageView);


        Label scoreLabel = new Label("Player Score:" + game.getScore().get());
        logger.info(game.getScore().get() + " This is the player score");
        scoreLabel.getStyleClass().add("myscore");
        scoreLabel.setAlignment(Pos.CENTER);
        scoreLabel.setTranslateY(-190);
        mainPane.setCenter(scoreLabel);


        ObservableList<Pair<String, Integer>> scores = loadScores("save.txt");

        localScores.setAll(scores);
        // binding the localScores to the ScoreList

        logger.info("scoreList added");
        //adding the scoreList to the root
        // Set the scene using the root pane

        Update(scores);

        // Setup for scores list
        localScoresList = new ScoresList(localScores);
        VBox scoreBox = new VBox(new Label("Local Scores"), localScoresList);
        scoreBox.getStyleClass().add("title");
        scoreBox.setTranslateX(70);
        scoreBox.setTranslateY(20);
        scoreBox.setAlignment(Pos.CENTER_LEFT);


        mainPane.setLeft(scoreBox);


        onlineScore = new ScoresList(remoteScores);
        VBox remoteScoreBox = new VBox(new Label("Online Scores"), onlineScore);
        logger.info(onlineScore + "this is the list");
        remoteScoreBox.getStyleClass().add("title");
        remoteScoreBox.setAlignment(Pos.CENTER_RIGHT);
        remoteScoreBox.setTranslateX(-70);
        remoteScoreBox.setTranslateY(20);

        mainPane.setLeft(scoreBox);
        mainPane.setRight(remoteScoreBox);


        logger.info("local scores " + localScores);

        Platform.runLater(() -> {
            localScoresList.reveal();
            onlineScore.reveal();
        });

    }


    /**
     * Loads scores from a particular file into an observable list
     *
     * @param filePath path of the string file
     * @return scores
     */
    public ObservableList<Pair<String, Integer>> loadScores(String filePath) {
        // creates an empty observableList scores to add in it the scoresList
        ObservableList<Pair<String, Integer>> scores = FXCollections.observableArrayList();

        try {
            // checks if a file exists or not if it doesn't exists then write the default names into the file
            if (!Files.exists(Paths.get(filePath))) {
                defaultWriter(filePath);
            }

            //using a bufferReader to read from the file
            try (BufferedReader bufferReader = new BufferedReader(new FileReader(filePath))) {
                String line;
                // if the file is not null read it line by line
                while ((line = bufferReader.readLine()) != null) {
                    // split each line on the colon
                    String[] nameScore = line.split(":");
                    // making sure that the splitting results in two parts name : score
                    if (nameScore.length == 2) {
                        // name is the firsts part
                        String name = nameScore[0];
                        // score is the second
                        int score = Integer.parseInt(nameScore[1].trim());
                        // adding the name and score to the scores observable list
                        scores.add(new Pair<>(name, score));
                    }


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to load scores: " + e.getMessage(), e);

        }

        scores.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        if (scores.size() > 10) {
            scores.remove(10, scores.size());
        }
        //return the scores
        return scores;
    }


    /**
     * Writes a list of scores of a specified file
     *
     * @param filePath the file where the scores will be written
     * @param scores   scores the list of name score to be written in the file
     */
    public void writerScores(String filePath, ObservableList<Pair<String, Integer>> scores) {
        logger.info("Starting to write to the file");

        //using a bufferWriter to write in the filePath
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath))) {
            //iterating through each score in the scores list
            for (Pair<String, Integer> score : scores) {
                // write each score in the format of "name:score"
                bufferedWriter.write(score.getKey() + ":" + score.getValue());
                // after writing move to a new line
                bufferedWriter.newLine();
            }
            logger.info("scores is written to the file successfully" + filePath);
        } catch (IOException e) {
            e.getMessage();
            logger.info("Error Occurred: " + e.getMessage());


        }


    }

    /**
     * Writes a default names to a particular file
     * it will appear if there is no file exists
     *
     * @param filePath a {@link java.lang.String} object
     */
    public void defaultWriter(String filePath) {
        logger.info("writing default scores ");
        // creating a new observableList for the defaultScores
        ObservableList<Pair<String, Integer>> defaultScores = FXCollections.observableArrayList();
        // those are the default names and scores for the default scores
        defaultScores.add(new Pair<>("Abdullah", 150));
        defaultScores.add(new Pair<>("Mohammed", 350));
        defaultScores.add(new Pair<>("Bob", 1000));
        defaultScores.add(new Pair<>("charlery", 700));
        defaultScores.add(new Pair<>("lol", 800));
        defaultScores.add(new Pair<>("faris", 1000));
        defaultScores.add(new Pair<>("abod", 600));
        defaultScores.add(new Pair<>("yosuf", 900));
        defaultScores.add(new Pair<>("yoyo", 1100));
        defaultScores.add(new Pair<>("bobe", 1200));

        // Sort the list by scores in descending order

        // This to sort the defaultScores in descending order
        Collections.sort(defaultScores, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // calling the writerScores method to write the names to the specified file
        writerScores("save.txt", defaultScores);
        logger.info("default names are written to the file successfully" + filePath);
    }


    /**
     * Checks if the current game scores is high score and updates the list accordingly
     *
     * @param scores The ObservableList to be updated
     */
    public void Update(ObservableList<Pair<String, Integer>> scores) {
        int currentScores = game.getScore().getValue();
        boolean newHighScore = false;

        // Loop through the list to find if the current score is higher than any previous scores
        for (int i = 0; i < scores.size(); i++) {
            if (currentScores > scores.get(i).getValue()) {
                newHighScore = true;
                break;
            }
        }

        if (newHighScore) {
            // Prompt the user for their name
            TextInputDialog name = new TextInputDialog("Enter Your Name:");
            name.setTitle("High Score!");
            name.setHeaderText("You got a high score enter your name please:");

            // Show the dialogue and wait for the player name
            Optional<String> result = name.showAndWait();
            result.ifPresent(resultName -> {
                // If the player enters a name, add the player name and score to the list
                scores.add(new Pair<>(resultName, currentScores));
                scores.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                if (scores.size() > 10) {
                    scores.remove(10, scores.size());
                }

                logger.info("A new name and Score is Added: " + resultName + ", " + currentScores);

                // Saving the new names and scores to a file
                writerScores("save.txt", scores);
                logger.debug("Scores updated");

                //check if the player score is higher than the online high score
                if (currentScores > getOnlineScores()) {

                    writeOnlineScore(resultName, currentScores);

                }
                // Animate the scores
                Platform.runLater(() -> {
                    localScoresList.update(scores);
                    localScoresList.reveal();
                });
            });
        }
    }


    /**
     * Returns the highest score from the online scores.
     *
     * @return the highest score if the list is not empty, otherwise 0.
     */
    public int getOnlineScores() {
        // Get the highest score from the online high scores
        if (!remoteScores.isEmpty()) {
            int highestOnlineScore = 0;
            for (Pair<String, Integer> score : remoteScores) {
                if (score.getValue() > highestOnlineScore) {
                    highestOnlineScore = score.getValue();
                }
            }
            return highestOnlineScore;
        }
        return 0; // Return 0 if there are no remote scores
    }


    /**
     * {@inheritDoc}
     * <p>
     * Receives messages from the communicator and processes them if they start with "HISCORES".
     */
    @Override
    public void receiveCommunication(String communication) {
        logger.info("Received Communication  " + communication);
        // checks if the received message starts with Hiscores
        if (communication.contains("HISCORES")) {
            //calling the setHighScore method to parse and update the highScore
            setHighScores(communication.substring("HISCORES".length()).trim());
            logger.info("HISCORES is rec and setHighScores should work ");
        } else if (communication.startsWith("NEWSCORE")) {
            // Parse the username and player score from the communication message
            String[] parts = communication.substring("NEWSCORE".length()).trim().split(":");
            if (parts.length == 2) {
                String userName = parts[0];
                int playerScore = Integer.parseInt(parts[1].trim());
                // Call the writeOnlineScore method with the parsed values
                writeOnlineScore(userName, playerScore);


            }
        }
    }


    /**
     * Send a request to load highScores
     */
    public void loadOnlineScores() {
        // sending the command for the highScores
        communicator.send("HISCORES");
    }


    /**
     * Parses the high scores from the received message and updates the remoteScores property.
     *
     * @param message the message containing the HISCORES
     */
    public void setHighScores(String message) {
        Platform.runLater(() -> {
            remoteScores.clear();  // Clear existing scores to avoid duplications
            String[] entries = message.trim().substring("HISCORES ".length()).split("\n");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    try {
                        int score = Integer.parseInt(parts[1].trim());
                        remoteScores.add(new Pair<>(parts[0], score));
                    } catch (NumberFormatException e) {
                        logger.error("Error parsing score: " + parts[1], e);
                    }
                }
            }
            remoteScores.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            logger.info(remoteScores.size() + " scores loaded");

            if (remoteScores.size() > 10) {
                remoteScores.remove(10, remoteScores.size());

            }
            onlineScore.update(remoteScores);


        });
    }


    /**
     * send to the server
     *
     * @param userName    the player name
     * @param playerScore the player Score
     */
    public void writeOnlineScore(String userName, int playerScore) {
        communicator.send(String.format("HISCORES" + userName + ":" + playerScore));
    }


    /**
     * esc
     */
    public void exitPressed() {

        game.goBackToMenu();
    }
}







