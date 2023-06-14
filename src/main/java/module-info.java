module music.example.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires org.controlsfx.controls;
    requires org.jfxtras.styles.jmetro;
    requires javafx.media;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;


    opens music.example.musicplayer to javafx.fxml;
    exports music.example.musicplayer;
}