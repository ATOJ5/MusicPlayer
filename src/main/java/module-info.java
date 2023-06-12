module music.example.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jfxtras.styles.jmetro;
    requires javafx.media;

    opens music.example.musicplayer to javafx.fxml;
    exports music.example.musicplayer;
}