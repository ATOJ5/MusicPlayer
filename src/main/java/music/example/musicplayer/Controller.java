package music.example.musicplayer;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller implements Initializable {
    @FXML
    private Label songLabel;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private ProgressBar songProgress;
    @FXML
    private Label songTime;
    @FXML
    private Button playButton,volumeButton,stopButton,resetButton,previousButton,nextButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private CheckBox shuffle,repeat;
    @FXML
    private FontAwesomeIcon icon,iconVolume;
    @FXML
    private ToggleButton expand;
    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber;
    private int[] speed = {25,50,75,100,125,150,175,200};
    private Timer timer;
    private TimerTask task;
    private boolean running=false;
    private boolean repeatON=false;
    private boolean shuffleON=false;
    private Media media;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isMuted = false;
    private TranslateTransition expandTransition;
    private TranslateTransition collapseTransition;

    private Stage stage;
    @Override
    public void initialize(URL arg0, ResourceBundle arg1){

        //Click to move progress song into a desired time.
        songProgress.setOnMouseClicked(event -> {
            double totalWidth = songProgress.getWidth();
            double clickPosition = event.getX();
            double percent = clickPosition / totalWidth;
            double seekTime = mediaPlayer.getTotalDuration().toSeconds() * percent;
            mediaPlayer.seek(Duration.seconds(seekTime));
        });

        // directory/files of songs
        songs = new ArrayList<>();
        directory = new File("Music");
        files = directory.listFiles();

        if(files !=null){
            for(File file: files){
                songs.add(file);
                System.out.println(file);
            }
        }
        insertSong();

        //Speed of the song
        for( int i = 0; i < speed.length; i++) {
            speedBox.getItems().add(Integer.toString(speed[i]));
        }
        speedBox.setOnAction(this::changeSpeed);

        expandTransition = new TranslateTransition(Duration.seconds(0.3), mainPane);
        expandTransition.setToY(200); // Expandir hacia abajo, ajusta el valor según sea necesario

        // Configurar la transición de contracción
        collapseTransition = new TranslateTransition(Duration.seconds(0.3), mainPane);
        collapseTransition.setToY(-mainPane.getPrefHeight());

        //volume
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {

                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });

        //Style of progress bar
        songProgress.setStyle("-fx-accent: #00ff00");
    }

    /**
     * Insert new songs from a list into media then building player with this list
     */
    private void insertSong() {
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        Platform.runLater(() -> {
            songLabel.setText(songs.get(songNumber).getName());
        });
    }
    public boolean shuffle() { return shuffle.isSelected();}

    public boolean repeat() {return repeat.isSelected();}

    /**
     * Enable/Disable repeat option
     */
    public void enDisRepeat() {
        if (repeat.isDisabled())
            repeat.setDisable(false);
        else
            repeat.setDisable(true);
    }

    public void endDisShuffle() {
        if (shuffle.isDisabled())
            shuffle.setDisable(false);
        else
            shuffle.setDisable(true);
    }
    /**
     * Play the current track. Begin timer and use the current speed. Also, get volume from slider then play the track
     */
    public void play() {

        if (timer != null) {
            endTimer();
           }
        beginTimer();
        changeSpeed(null);
        if(!isMuted) {
            mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
        } else {
            mediaPlayer.setVolume(0);
        }
        mediaPlayer.play();
    }

    public void muteOnOff(){

        volumeButton.setDisable(true);

        if (isMuted){
            muteOff();

        } else {
            muteOn();
        }
        volumeButton.setDisable(false);
    }

    private void muteOn() {
        isMuted = true;
        iconVolume.setGlyphName("VOLUME_OFF");
        mediaPlayer.setVolume(0);
    }

    private void muteOff() {
        isMuted = false;
        iconVolume.setGlyphName("VOLUME_UP");
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
    }

    public void playPause() {

        playButton.setDisable(true);

        if (isPlaying) {
            isPausing();
        } else {
            isPlaying();
        }
        playButton.setDisable(false);
    }

    private void isPlaying() {
        isPlaying = true;
        icon.setGlyphName("PAUSE");
        play();
    }

    private void isPausing() {
        isPlaying = false;
        icon.setGlyphName("PLAY");
        pause();
    }

    public void stop() {
        endTimer();
        if (!isPlaying){
            isPlaying();
        }
        songProgress.setProgress(0);
        playPause();
        mediaPlayer.stop();
    }
    public void pause() {
        mediaPlayer.pause();
    }

    public void reset() {
        mediaPlayer.seek(Duration.seconds(0));
        if (!isPlaying){
            isPlaying();
        }
        play();
    }
    public void previous(){

        if (shuffle() && !repeat()) {
            shuffleAct();
        } else if (!repeat()) {
            sequentialBack();
        } else if (repeat()) {
            repeatAct();
        }
        if (running) {
            endTimer();
        }
        if (!isPlaying){
            isPlaying();
        }
        play();
    }

    public void next() {
        if (shuffle() && !repeat()) {
            shuffleAct();
        } else if (!repeat()) {
            sequentialNext();
        } else if (repeat()) {
            repeatAct();
        }
        if (running) {
            endTimer();
        }
        if (!isPlaying){
            isPlaying();
        }
        play();
    }

    /**
     * Selecting a random song from the list of songs
     */
    private void shuffleAct() {
        songNumber = (int) (Math.random() * songs.size());
        mediaPlayer.stop();
        insertSong();
        endTimer();

    }
    private void sequentialBack(){

        if (songNumber == 0) {
            songNumber = songs.size() - 1;
        } else {
            songNumber--;
        }
        mediaPlayer.stop();
        insertSong();
    }
    private void sequentialNext() {
        if (songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        mediaPlayer.stop();
        insertSong();
        endTimer();
    }

    private void repeatAct() {
        reset();
        play();
    }

    public void changeSpeed (ActionEvent event) {

        if (speedBox.getValue() == null) {
            speedBox.setValue("100");
        }
        mediaPlayer.setRate(Integer.parseInt(speedBox.getValue()) * 0.01);
    }

    public void expandCollapse(){

    }

    public void beginTimer() {

        timer = new Timer();
        task = new TimerTask() {

            public void run() {
                running = true;
                if (mediaPlayer != null) {
                    Duration current = mediaPlayer.getCurrentTime();
                    Duration total = mediaPlayer.getTotalDuration();
                    if (current.equals(total)){
                        endTimer();
                        next();
                    } else {
                        double progress = current.toSeconds() / total.toSeconds();
                        Platform.runLater(() -> {
                            songProgress.setProgress(progress);
                        });
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void endTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

    public void openFile() {System.out.println("File opened!");}
    public void openFolder() {System.out.println("Folder opened!");}
}