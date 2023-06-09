package music.example.musicplayer;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import java.io.File;
import java.net.URL;
import java.util.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Controller implements Initializable {
    @FXML
    private Label songLabel,trackLabel;
    @FXML
    private AnchorPane aboutPane;
    @FXML
    private ProgressBar songProgress;
    @FXML
    private Label currentTimeLabel,totalTimeLabel;
    @FXML
    private Button playButton,volumeButton,minimizeButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private CheckBox shuffle,repeat;
    @FXML
    private FontAwesomeIcon icon,iconVolume,iconDown;
    private File directory = new File("Music");
    private File[] defaultDirectory = directory.listFiles();
    private ArrayList<File> songList = new ArrayList<>();
    private ArrayList<File> favouriteSongList = new ArrayList<>();
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
    private boolean isExpanded = false;
    private Stage stage;
    private File file;
    private File selectedDirectory;
    private double xOffset;
    private double yOffset;
    @FXML
    private ListView<File> songListView;
    @FXML
    public FontAwesomeIcon heart;
    @FXML
    private ScrollBar scrollBar;
    private ObservableList<File> items;
    private boolean favouriteIsOn=false;
    private HostServices hostServices;
    private IntegerProperty activeSongIndex = new SimpleIntegerProperty(-1);

    @Override
    public void initialize(URL arg0, ResourceBundle arg1){

        addSongs();
        if (defaultDirectory != null) {
            insertSong();
        }

        //Listing our files to Track List
        ArrayList<File> listaCanciones = songList;
        items = FXCollections.observableArrayList(listaCanciones);
        songListView.setItems(items);
        songListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectSong(newValue);
            activeSongIndex.set(songListView.getSelectionModel().getSelectedIndex());
        });


        //removing .mp3 text and directories for sharing only track titles.
        songListView.setCellFactory(param -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getName().replace(".mp3", ""));
                    int currentIndex = getIndex();
                    if (currentIndex == activeSongIndex.get()) {
                        setStyle("-fx-underline: true;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Set the initial active song index
        activeSongIndex.set(songListView.getSelectionModel().getSelectedIndex());

        // Configure the listener for the media player status
        activeSong();

        setupScrollBar();

        //Speed of the song
        for( int i = 0; i < speed.length; i++) {
            speedBox.getItems().add(Integer.toString(speed[i]));
        }
        speedBox.setOnAction(this::changeSpeed);

        //Style of progress bar
        songProgress.setStyle("-fx-accent: #00ff00");
    }
    @FXML
    private void handleVolumeSliderMouseDragged(MouseEvent event) {
        double volume = volumeSlider.getValue() * 0.01;
        mediaPlayer.setVolume(volume);
    }
    private void addSongs() {
        if(defaultDirectory !=null){
            for(File file: defaultDirectory){
                songList.add(file);
            }
        }
    }
    private void activeSong() {
        mediaPlayer.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == MediaPlayer.Status.PLAYING) {
                int selectedIndex = songListView.getSelectionModel().getSelectedIndex();
                songListView.getSelectionModel().clearAndSelect(selectedIndex);
            }
        });
    }

    private void insertSong() {
        media = new Media(songList.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        totalDurationSong();
        replaceMp3Format();
    }
    private void totalDurationSong() {
        mediaPlayer.setOnReady(() -> {
            Duration duration = mediaPlayer.getTotalDuration();
            totalTimeLabel.setText(formatDuration(duration));
        });
    }
    private void replaceMp3Format(){
        Platform.runLater(() -> {
            songLabel.setText(songList.get(songNumber).getName().replace(".mp3",""));
        });
    }

    private void setupScrollBar() {

        scrollBar.setMin(0);
        scrollBar.setMax(songList.size() - 1);
        scrollBar.setVisibleAmount(1);
        scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            int selectedIndex = newValue.intValue();
            songListView.getSelectionModel().clearAndSelect(selectedIndex);
        });
    }

    private String formatDuration(Duration duration) {
        long minutes = (long) duration.toMinutes();
        long seconds = (long) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void currentTimeLabel (){
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            currentTimeLabel.setText(formatDuration(newValue));
        });
    }

    private void favouriteTrackDirectory() {
        items.setAll(songList);
        Platform.runLater(() -> {
            trackLabel.setText("FAVOURITE TRACKS");
        });
    }


    public void moveSong(MouseEvent event) {

        double totalWidth = songProgress.getWidth();
        double clickPosition = event.getX();
        double percent = clickPosition / totalWidth;
        double seekTime = mediaPlayer.getTotalDuration().toSeconds() * percent;
        mediaPlayer.seek(Duration.seconds(seekTime));
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
        checkColor();
        currentTimeLabel();
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

    private void shuffleAct() {
        songNumber = (int) (Math.random() * songList.size());
        mediaPlayer.stop();
        insertSong();
        endTimer();
    }
    private void sequentialBack(){

        if (songNumber == 0) {
            songNumber = songList.size() - 1;
        } else {
            songNumber--;
        }
        mediaPlayer.stop();
        insertSong();
    }
    private void sequentialNext() {
        if (songNumber < songList.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        mediaPlayer.stop();
        insertSong();

        endTimer();
    }

    private void selectSong(File song) {

        if (song != null){
            for ( int i = 0; i < songList.size(); i++ ) {
                if (songList.get(i).getName().equals(song.getName())){
                    songNumber = i;
                    mediaPlayer.stop();
                    endTimer();
                    insertSong();
                    play();
                }
            }
        }
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
    public void adjustWindowSize() {

        if (!isExpanded){
            expand();
        } else {
            collapse();
        }
    }
    public void expand() {
        isExpanded = true;
        stage.setHeight(500);
        iconDown.setGlyphName("CHEVRON_UP");
    }

    public void collapse(){

        isExpanded=false;
        stage.setHeight(225);
        iconDown.setGlyphName("CHEVRON_DOWN");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public void openFile() {

        FileChooser fileChooser = new FileChooser();
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            stop();
            songList.clear();
            songList.add(file);
            insertSong();
        }
    }

    public void selectedDirectory() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(stage);
        openFolder(selectedDirectory);
    }

    public void selectedFavouriteSongs(){

        if(favouriteSongList.size() > 0){
            stop();
            songList.clear();
            favouriteIsOn = true;
            songList.addAll(favouriteSongList);
            favouriteTrackDirectory();
            insertSong();


        } else {
            System.out.println("There aren't any favourite songs ");
        }
    }

    public void openFolder(File selectedDirectory) {

        if (selectedDirectory != null) {
            stop();
            songList.clear();
            directory = selectedDirectory;
            defaultDirectory = directory.listFiles();
            favouriteIsOn = false;
            addSongs();
            insertSong();
            items.setAll(songList);

        }
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    private void minimizeButtonClicked() {
        minimizeWindow();
    }

    private void minimizeWindow() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void logOut() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("Press OK to log out, or cancel to stay in the application.");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("main.css").toExternalForm());

        if (alert.showAndWait().get() == ButtonType.OK) {
            System.out.println("Log Out");
            endTimer();
            stage.close();
        }
    }

    @FXML
    public void showAboutWindow() {

        aboutPane.setVisible(true);
    }

    @FXML
    public void hideAboutWindow() {
        aboutPane.setVisible(false);
    }

    public void clickHearth() {

        File songFile = obtenerCancionActual();
        boolean found = false;
        found = dislike(found);
        like(songFile, found);


    }

    private void like(File songFile, boolean found) {
        if (!found) {
            favouriteSongList.add(songFile);
            heart.setFill(Color.RED);
            if (favouriteIsOn){
                items.setAll(favouriteSongList);
            }
        }
    }

    private boolean dislike(boolean found) {

        Iterator<File> iterator = favouriteSongList.iterator();

        while (iterator.hasNext()) {
            File file = iterator.next();
            if (file.getName().equals(obtenerCancionActual().getName())) {
                iterator.remove();
                found = true;
                heart.setFill(Color.WHITE);
                if (favouriteIsOn){
                    items.setAll(favouriteSongList);
                }
                break;
            }
        }
        return found;
    }

    public void checkColor() {
        for (File favourite : favouriteSongList) {
            if (favourite.getName().equals(obtenerCancionActual().getName())) {
                heart.setFill(Color.RED);
                return;
            }
        }
        heart.setFill(Color.WHITE);
    }
    private File obtenerCancionActual() {
        if (songNumber < songList.size()) {
            return songList.get(songNumber);
        }
        return null;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void openYoutubeLink () {

        String videoTitle = obtenerCancionActual().getName().replace(".mp3","");
        String youtubeLink = "https://www.youtube.com/results?search_query=" + videoTitle.replace(" ", "+");

        if (hostServices != null) {
            hostServices.showDocument(youtubeLink);
        }
    }
    public void openSpotifyLink() {

        String videoTitle = obtenerCancionActual().getName().replace(".mp3","");
        String spotifyLink = "https://open.spotify.com/search/" + videoTitle.replace(" ", "%20");

        if (hostServices != null) {
            hostServices.showDocument(spotifyLink);
        }
    }

}

