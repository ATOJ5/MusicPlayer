package music.example.musicplayer;

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
    private CheckBox shuffle;
    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber;
    private int[] speed = {25,50,75,100,125,150,175,200};
    private Timer timer;
    private TimerTask task;
    private boolean running;
    private Media media;
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1){

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
    public boolean shuffle() {
        return shuffle.isSelected();
    }
    private void insertSong() {
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        songLabel.setText(songs.get(songNumber).getName());
    }

    public void play() {

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
    }
    public void previous(){

        if (shuffle()) {
            songNumber = (int) (Math.random() * songs.size());
            mediaPlayer.stop();
            insertSong();
        } else {
            if (songNumber == 0 ) {
                songNumber = songs.size() -1;
                mediaPlayer.stop();
                insertSong();

            } else {
                songNumber--;
                mediaPlayer.stop();
                insertSong();
            }
        }
        if (running){
            endTimer();
        }
        play();

    }

    public void next() {

        if (shuffle()) {
            songNumber = (int) (Math.random() * songs.size());
            mediaPlayer.stop();
            insertSong();
        } else {
            if (songNumber < songs.size() -1 ) {
                songNumber++;
                mediaPlayer.stop();
                insertSong();

            } else {
                songNumber = 0;
                mediaPlayer.stop();
                insertSong();
            }
        }
        if (running){
            endTimer();
        }
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
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = mediaPlayer.getTotalDuration().toSeconds();
                songProgress.setProgress(current/end);

                if (current/end == 1){
                    endTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void endTimer() {
        timer.cancel();
        running = false;
    }

    public void openFile() {
        System.out.println("File opened!");
    }
    public void openFolder() {
        System.out.println("Folder opened!");
    }



}