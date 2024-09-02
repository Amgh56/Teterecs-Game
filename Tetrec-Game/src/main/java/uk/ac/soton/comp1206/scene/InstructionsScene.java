package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The instructionsScene class represent the scene that displays the instructions to the player.
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class InstructionsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    // this instance variable is to let the small grids in the instructions scene split into 6 columns
    private final int numColumns = 5;
    // and this adjust the height and and width of the instruction image
    private final int widthAndHeightOfTheBoard = 50;

    private PieceBoard pieceBoard;


    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);

    }

    /**
     * {@inheritDoc}
     * <p>
     * initialise the scene with event handlers
     */
    @Override
    public void initialise() {

        //it sets a events handler within the key presses
        // if the player pressed esc then the game exits and go back to the menu
        this.scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                exitPressed();
            }
        });
    }


    /**
     * {@inheritDoc}
     * <p>
     * build the instructions scene
     */
    @Override
    public void build() {

        logger.info("Building the instructions scene");

        // creating a game root and setting its width and height to aligns with the Game Window
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        // a mainPain that will contain all the ui elements
        var mainPain = new StackPane();
        // style the mainPain
        mainPain.getStyleClass().add("menu-background");
        // adding it to the root
        root.getChildren().add(mainPain);

        // A instructionPane which is responsible about the image and the image vbox which is the text
        var instructionPane = new BorderPane();

        /*
        Loading the instructions image and setting the width and height as the same as the gameWindow so it fits
         the scene and assign it to true, so it be more clear
         */
        Image image = new Image(InstructionsScene.class.getResource("/images/Instructions.png").toExternalForm(), gameWindow.getWidth(), gameWindow.getHeight(), true, true);
        ImageView imageView = new ImageView(image);
        // set the preserve ratio to true to maintain its width and height
        imageView.setPreserveRatio(true);
        // here i am dividing the width of the image so it fits the scene
        imageView.setFitWidth(gameWindow.getWidth() / 1.5);
        logger.info("image loaded ");
        //GridPane for laying out the game pieces
        GridPane gridPane = new GridPane();
        // setting spaces horizontally and vertically between the grids
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        // setting the grid to the center of the scene
        gridPane.setAlignment(Pos.CENTER);


        // create each grid with images for each shape
        for (int i = 0; i < GamePiece.PIECES; i++) {
            // creating a new piece for the whole 15 pieces
            GamePiece piece = GamePiece.createPiece(i);
            // Create a board for each piece
            PieceBoard board = new PieceBoard(widthAndHeightOfTheBoard, widthAndHeightOfTheBoard, false);
            // Associate the PieceBoard with the GamePiece
            board.settingPieceToDisplay(piece);

            /*
             Calculate the position of the board in the grid and add it to the GridPane so
              to let the whole pieces appear in a particular columns and rows
              */
            gridPane.add(board, i % numColumns, i / numColumns);

        }


        // this to arrange the paddings of each edge top,bottom,left,right
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        //adding a new text in the title
        Text instructionTitle = new Text("Instructions");
        // styling the text
        instructionTitle.getStyleClass().add("heading");
        //setting the spaces
        var imageVbox = new VBox(20);
        // adding the title to the vbox and arranging its position
        imageVbox.getChildren().add(instructionTitle);
        imageVbox.setAlignment(Pos.TOP_CENTER);
        imageVbox.getChildren().add(imageView);


        // adding a title to the grid pieces
        Text gridTitle = new Text("Game Piece");
        // styling it
        gridTitle.getStyleClass().add("heading");
        // adding spaces
        var gridVbox = new VBox(20);
        // adding the title to the grid vbox and arranging it
        gridVbox.getChildren().add(gridTitle);
        gridVbox.getChildren().add(gridPane);
        gridVbox.setAlignment(Pos.BOTTOM_CENTER);


        // this is to raise the image up
        imageView.setTranslateY(-110);

        // setting the image and title at the center of the instructionPane
        instructionPane.setCenter(imageView);
        instructionPane.setTop(imageVbox);


        // adding the grid title and instruction pane to the mainPain
        mainPain.getChildren().add(gridVbox);
        mainPain.getChildren().add(instructionPane);

        //making sure all of them are arranged in the way i want
        StackPane.setAlignment(instructionPane, Pos.CENTER);
        StackPane.setAlignment(mainPain, Pos.BOTTOM_CENTER);


    }

    /**
     * A method for exiting the instructions
     */
    public void exitPressed() {
        // this what returns from the instructions to the menu page

        gameWindow.startMenu();
    }


}

