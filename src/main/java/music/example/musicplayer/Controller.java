package music.example.musicplayer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    private Button playButton,stopButton,resetButton,previousButton,nextButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private CheckBox shuffle,repeat;
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

        for( int i = 0; i < speed.length; i++) {
            speedBox.getItems().add(Integer.toString(speed[i]));
        }

        speedBox.setOnAction(this::changeSpeed);
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {

                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });
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
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
        mediaPlayer.play();
    }

    public void stop() {
        endTimer();
        mediaPlayer.stop();
    }
    public void pause() {
        songProgress.setProgress(0);
        mediaPlayer.pause();
    }

    public void reset() {
        mediaPlayer.seek(Duration.seconds(0));
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

    public void endTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

    public void openFile() {System.out.println("File opened!");}
    public void openFolder() {System.out.println("Folder opened!");}
}