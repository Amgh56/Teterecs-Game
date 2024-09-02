package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * <p>LobbyScene class.</p>
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class LobbyScene extends BaseScene {
    private Communicator communicator;
    private Timer channelUpdateTimer;
    public SimpleStringProperty currentChannel;
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    private ListView<String> userList;
    private ListView<String> messageList;
    private VBox channelContainer; // You can also use TilePane or another Pane if you prefer


    private TextArea chatDisplay = new TextArea();
    private TextField chatInput = new TextField();

    private Button startGameButton;


    // Initialize lists for data
    private ObservableList<String> channelData = FXCollections.observableArrayList();
    private ObservableList<String> userData = FXCollections.observableArrayList();
    private ObservableList<String> messageData = FXCollections.observableArrayList();

    private boolean host;
    private boolean join;

    private TextField messageInput;


    /**
     * <p>Constructor for LobbyScene.</p>
     *
     * @param gameWindow a {@link uk.ac.soton.comp1206.ui.GameWindow} object
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        this.communicator = gameWindow.getCommunicator();

        this.currentChannel = new SimpleStringProperty();
        this.channelContainer = new VBox();
        this.startGameButton = new Button();


        // Initialize components
        userList = new ListView<>(userData);
        messageList = new ListView<>(messageData);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialise() {
        handleKeyPresses();
        listenToServerMessages();
        ChannelUpdateTimer();
        this.scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                exitToMenu();
            }
        });
    }

    private void handleKeyPresses() {
        this.scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                exitLobby();
            }
        });
    }


    /**
     * This request the server for the list every 5 seconds
     */
    public void ChannelUpdateTimer() {
        // Create a TimerTask that defines what to do when the timer is triggered
        TimerTask requestUpdateTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("latest Channel List");
                //send a List Command to the server to ask for the list
                sendMessageToServer("LIST");
            }
        };
        channelUpdateTimer = new Timer("ChannelUpdater");
        // Schedule the previously defined TimerTask to run immediately, and then every 5000 milliseconds (5 seconds)
        channelUpdateTimer.scheduleAtFixedRate(requestUpdateTask, 0, 5000);
    }

    /**
     * This method ensures that any messages received from the server is shown in the ui
     */
    public void listenToServerMessages() {
        this.communicator.addListener(serverMessage -> {
            Platform.runLater(() -> handleServerMessage(serverMessage.trim()));
        });
    }


    /**
     * Processes and routes server messages to appropriate methods.
     *
     * @param message The server message with a command and data.
     */
    public void handleServerMessage(String message) {
        logger.info("Server message received: " + message);
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "CHANNELS":
                updateChannelList(data);
                break;
            case "JOIN":
                updateCurrentChannel(data);
                requestUsersInChannel(data);
                break;
            case "USERS":
                updateUsersList(data);
                break;
            case "MSG":
                displayMessage(data);
                break;
            case "PARTED":
                clearCurrentChannel();
                break;
            case "HOST": // Example command indicating the user is now the host
                Platform.runLater(() -> startGameButton.setVisible(true));
                break;
            case "ERROR":
                showError(data);
                break;
            default:
                logger.warn("Unhandled server message: " + command);
                break;
        }
    }

    /**
     * Updates the list of channels on the UI from a newline-separated string of channel names.
     *
     * @param channelData String containing channel names separated by newlines.
     */
    public void updateChannelList(String channelData) {
        Platform.runLater(() -> {
            List<String> channels = Arrays.asList(channelData.split("\n"));
            this.channelContainer.getChildren().clear(); // Clear existing buttons
            for (String channelName : channels) {
                Button channelButton = new Button(channelName);
                channelButton.getStyleClass().add("host");
                channelButton.setOnAction(e -> joinChannel(channelName));
                this.channelContainer.getChildren().add(channelButton);
            }
        });
    }


    /**
     * <p>joinChannel.</p>
     *
     * @param channelName a {@link java.lang.String} object
     */
    public void joinChannel(String channelName) {
        // If in another channel, first send a leave command

        if (!join) {
            sendMessageToServer("JOIN " + currentChannel.get());
        }

        // Then join the new channel
        currentChannel.set(channelName); // Update the current channel property

        logger.info(" you are in this channel " + channelName);
    }


    /**
     * <p>requestUsersInChannel.</p>
     *
     * @param channelName a {@link java.lang.String} object
     */
    public void requestUsersInChannel(String channelName) {
        sendMessageToServer("USERS " + channelName);
    }


    /**
     * <p>sendMessage.</p>
     */
    public void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            sendMessageToServer("MSG " + message);
            chatInput.clear();
        }
    }


    /**
     * Sets the current channel to the specified channel name.
     * Updates are made on the JavaFX Application Thread to ensure thread safety with UI components.
     *
     * @param channelName a {@link java.lang.String} object
     */
    public void updateCurrentChannel(String channelName) {

        currentChannel.set(channelName);

    }


    /**
     * Updates the userList with newline-separated user data.
     *
     * @param userData String of user names separated by newlines.
     */
    public void updateUsersList(String userData) {
        Platform.runLater(() -> {
            List<String> users = Arrays.asList(userData.split("\n"));
            this.userData.setAll(users);

            // Assuming you have a ListView<String> userList;
            userList.setItems(this.userData); // Bind the observable list to the ListView

            // Apply a custom style class to the userList to make it blend with the background

        });
    }

    /**
     * Adds a received message to the observable list for display in the UI.
     * Ensures that the update is made on the JavaFX Application Thread for thread safety.
     *
     * @param message The message to be added to the display list.
     */
    public void displayMessage(String message) {

        chatDisplay.appendText(message + "\n");


    }

    private void clearChatDisplay() {
        chatDisplay.clear();
    }


    /**
     * <p>leaveChannel.</p>
     */
    public void leaveChannel() {
        sendMessageToServer("PART " + currentChannel.get());
        currentChannel.set(null);

    }


    /**
     * Resets the current channel to null on the JavaFX Application Thread.
     */
    private void clearCurrentChannel() {
        Platform.runLater(() -> {
            currentChannel.set(null);
        });
    }


    /**
     * Exits the lobby by sending a part message if a channel is currently joined, terminates all activities,
     * and goes back to the menu.
     */
    private void exitLobby() {
        if (this.currentChannel.isNotEmpty().get()) {
            this.communicator.send("PART");
        }
        terminateActivities();
        this.gameWindow.startMenu();
    }


    /**
     * <p>sendMessageToServer.</p>
     *
     * @param message the messages sent to the server
     */
    public void sendMessageToServer(String message) {
        this.communicator.send(message);
    }


    /**
     * Displays an error alert with the given message on the JavaFX Application Thread.
     *
     * @param errorMessage The message to be displayed in the alert.
     */
    public void showError(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
            alert.showAndWait();
        });
    }


    /**
     * Stops and cleans up the channel update timer.
     * Ensures no ongoing tasks are left running when the lobby scene is exited.
     */
    public void terminateActivities() {
        if (channelUpdateTimer != null) {
            channelUpdateTimer.cancel();
            channelUpdateTimer = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {
        // Layout building logic
        // creating a game root and setting its width and height to aligns with the Game Window
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        // a mainPain that will contain all the ui elements
        var mainPane = new StackPane();
        // style the mainPain
        mainPane.getStyleClass().add("menu-background");
        // adding it to the root
        root.getChildren().add(mainPane);

        Label label = new Label("Multiplayer");
        label.getStyleClass().add("LobbyScene");
        mainPane.getChildren().add(label);
        mainPane.setAlignment(Pos.TOP_CENTER);

        Label label1 = new Label("Current Games");
        label1.getStyleClass().add("myscore");
        label1.setAlignment(Pos.TOP_LEFT);
        label1.setTranslateX(-270);
        label1.setTranslateY(70);
        mainPane.getChildren().add(label1);


        VBox layout = new VBox(10);
        layout.setAlignment(Pos.TOP_LEFT);
        Button newChannelButton = new Button("Host new Game");
        newChannelButton.getStyleClass().add("host");
        layout.setTranslateY(120);
        layout.setTranslateX(30);
        newChannelButton.setOnAction(e -> promptForNewChannel());
        // Add new channel button to layout
        layout.getChildren().add(newChannelButton);

        layout.getChildren().add(channelContainer);
        mainPane.getChildren().add(layout);
    }


    private void promptForNewChannel() {
        if (!join) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Channel");
            dialog.setHeaderText("Create a New Channel");
            dialog.setContentText("Enter channel name:");

            // Traditional way to get the response value.
            dialog.showAndWait().ifPresent(channelName -> {
                if (!channelName.trim().isEmpty()) {
                    sendMessageToServer("CREATE " + channelName);
                    currentChannel.set(channelName);
                } else {
                    showError("Channel name cannot be empty.");
                }
            });
        }
    }

    /**
     * if ESCAPE is pressed it goes out of the multiplier go back to the menu
     */
    public void exitToMenu() {
        gameWindow.startMenu();

    }


}
