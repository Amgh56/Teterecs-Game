package uk.ac.soton.comp1206.game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class to handle the media functionality such as menu sound effects
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public class Multimedia {

    /**
     * logger to debug the errors and print useful statements
     */
    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    /**
     * This is an instance variable for playing short sound effects
     */
    protected static MediaPlayer audio;

    /**
     * This is an instance variable for playing background music like the menu music
     */
    protected static MediaPlayer menuMusic;


    /**
     * Plays an audio from the /sounds/ directory
     *
     * @param sounds this is the file name of the file to play
     */
    public void playAnAudio(String sounds) {
        // Construct a URL for the sound file
        String media = Multimedia.class.getResource("/sounds/" + sounds).toExternalForm();

        try {
            // create a media object from the URL
            Media playAudio = new Media(media);
            //assign the MediaPlayer with the media object
            audio = new MediaPlayer(playAudio);
            // play the audio
            audio.play();

            // checking the sounds played
            logger.info("Playing an audio: " + sounds);

        } catch (Exception e) {
            // print the stack trace if there is an error
            e.printStackTrace();
            logger.error("Error: " + sounds, e);

        }


    }

    /**
     * This method plays the background music in from the /music/ directory
     *
     * @param music this is the file name of the file to play the music
     */
    public void playAnBackgroundMusic(String music) {
        // construct the URL from the music file
        String backGroundMusic = Multimedia.class.getResource("/music/" + music).toExternalForm();


        try {
            // Create a media object for the background music
            Media media = new Media(backGroundMusic);
            // assign the mediaPlayer with the media object
            menuMusic = new MediaPlayer(media);
            // set the game to autoplay when the mediaPlayer is ready
            menuMusic.setAutoPlay(true);
            // let the music run definitely until it gets stopped
            menuMusic.setCycleCount(menuMusic.INDEFINITE);
            //play the background music
            menuMusic.play();

            logger.info("Playing the background music: " + music);


        } catch (Exception e) {
            // print the stack trace if there is an error
            logger.error("Error: " + music, e);
            e.printStackTrace();
        }


    }

    /**
     * stops the currently playing background music
     */
    public void backGroundMusicStop() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic = null; // Optionally reset to null after stopping if you don't plan to reuse it
            logger.info("Background music stopped.");
        } else {
            logger.warn("Attempted to stop background music, but no music was playing.");
        }
    }


}
