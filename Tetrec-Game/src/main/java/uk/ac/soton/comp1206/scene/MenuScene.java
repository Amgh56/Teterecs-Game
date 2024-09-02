package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Key;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private static Multimedia multimediaSounds = new Multimedia();


    /**
     * Create a new menu scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");

    }

    /**
     * {@inheritDoc}
     * <p>
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);


        // creating the menu buttons
        var singlePlayerbutton = new Button("Single Player");
        var multiplierButton = new Button("Multi Player");
        var instructionsButton = new Button("Instructions");
        var exit = new Button("Exit");

        // setting the buttons into action and applying the css styling on them
        singlePlayerbutton.setOnAction(this::startGame);
        singlePlayerbutton.getStyleClass().add("menuItem");
        multiplierButton.setOnAction(event -> gameWindow.startMultiplyer());
        multiplierButton.getStyleClass().add("menuItem");
        instructionsButton.setOnAction(event -> gameWindow.startInstruction());
        instructionsButton.getStyleClass().add("menuItem");
        exit.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                exitGame();
            }
        });

        exit.setOnAction(event -> Platform.exit());
        exit.getStyleClass().add("menuItem");

        logger.info("Menu buttons created");


        // load an image from a url folder by using getResource and the path name to a string representation by using toExternalForm
        Image image = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
        //This creates an Image view
        ImageView imageView = new ImageView(image);
        // Rotate the image and choosing the seconds the rotation lasts
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), imageView);
        // The angle to rotate by 15 degree
        rotateTransition.setByAngle(15);
        // setting the CycleCount to infinity
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        // rotation in a linear time in a constant time
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        // auto reversing the logo
        rotateTransition.setAutoReverse(true);
        // This sets the Height of the images
        imageView.setFitHeight(100);
        //This sets the Width of the images
        imageView.setFitWidth(600);
        // aligns the image to the center
        imageView.setTranslateY(100);
        BorderPane.setAlignment(imageView, Pos.CENTER);
        // playing the rotation
        rotateTransition.play();

        logger.info("Title animation ");

        // a Vbox created for the menu buttons and spacing each button by five pixels
        var buttons = new VBox(5);
        buttons.setAlignment(Pos.CENTER);
        buttons.setTranslateY(50);
        buttons.getChildren().addAll(singlePlayerbutton, multiplierButton, instructionsButton, exit);
        // adding the buttons to  the center of the mainPane and set alignment to the center
        mainPane.setCenter(buttons);
        // adding padding inside the border
        mainPane.setPadding(new Insets(10));
        //  setting the title to the top of the mainPane
        mainPane.setTop(imageView);


        logger.info("Menu Scene built successfully ");

    }


    /**
     * {@inheritDoc}
     * <p>
     * Initialise the menu
     */
    @Override
    public void initialise() {
        multimediaSounds.playAnBackgroundMusic("menu.mp3");

        this.scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                exitGame();
            }
        });

    }

    /**
     * Handle when the Start Game button is pressed
     *
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        multimediaSounds.backGroundMusicStop();

    }

    /**
     * Handle when the instructions button is pressed
     */
    public void exitGame() {
        Platform.exit();
        System.exit(0);


    }

}
