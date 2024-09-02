package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class generate the list of the scores
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class ScoresList extends BorderPane {


    private static final Logger logger = LogManager.getLogger(ScoresList.class);

    // A vbox to show the scores
    private VBox scoresBox;

    // scoresProperty for the players
    private ListProperty<Pair<String, Integer>> scoresProperty;


    /**
     * Constructs a ScoresList component that displays scores from the provided observable list.
     * Initializes the component and sets up the UI to display the scores.
     *
     * @param scoreList The observableList
     */
    public ScoresList(ObservableList<Pair<String, Integer>> scoreList) {
        // A message to make sure the  scorelist component is ready
        logger.info("Initializing ScoresList Component");

        //Set up the list property that will handle the scores to make sure it can react with the UI
        scoresProperty = new SimpleListProperty<>(scoreList);
        //the vbox that will contain the scores entries
        scoresBox = new VBox(5);
        //set the vbox to the center
        scoresBox.setAlignment(Pos.CENTER);

        // populate the scores into the vbox
        processScores();

        // Set the vbox to the center of the ui
        this.setCenter(scoresBox);
        this.getStyleClass().add("scorelist");
    }

    /**
     * Clears existing score entries and repopulates the scoresBox with updated score entries.
     */
    private void processScores() {
        //clear any existing content
        scoresBox.getChildren().clear();

        //iterate through each score in the scoresProperty and create  ui element
        for (Pair<String, Integer> score : scoresProperty) {
            HBox scoreEntry = new HBox(10);
            scoreEntry.setAlignment(Pos.CENTER);

            //create a Text for the player Name
            Text name = new Text(score.getKey());
            // create a Text points for the player
            Text points = new Text(": " + score.getValue().toString());
            //styling
            name.getStyleClass().add("scorelist");
            points.getStyleClass().add("scorelist");

            // and the name and points to the hbox
            scoreEntry.getChildren().addAll(name, points);
            //add the hbox to the vbox
            scoresBox.getChildren().add(scoreEntry);
        }
        logger.info("Scores successfully processed");
        numberOfElement();


    }

    /**
     * Updates the list of the scores displayed by this component
     *
     * @param newScores the new list to be processed
     */
    public void update(ObservableList<Pair<String, Integer>> newScores) {
        // making the updates run in the ui
        Platform.runLater(() -> {
            //updates the scoresProperty
            scoresProperty.set(FXCollections.observableArrayList(newScores));
            //refresh the scoresProperty of the scores
            processScores();
            logger.info("Scores updated and UI should refresh.");

        });
    }

    /**
     * Ensures the list maintains only the top 10 scores.
     */
    private void numberOfElement() {
        // check if the scoresProperty is more than 10
        if (scoresProperty.getSize() > 10) {
            // Sort the scores first to ensure the lowest scores can be trimmed
            FXCollections.sort(scoresProperty, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            // Remove the lowest scores beyond the 10th entry
            while (scoresProperty.getSize() > 10) {
                scoresProperty.remove(10);
            }
        }
    }

    /**
     * Animates the scores by setting the opacity from 0 to 1
     */
    public void reveal() {
        logger.info("scoreBox reveal");
        // setting fadeTransition for the scoresBox
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(2000), scoresBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
        logger.info("scores Animation Reveal started ");
    }


}

