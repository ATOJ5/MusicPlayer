package music.example.musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;

public class MainAPP extends Application {

    public static Controller controller;
    public double height = 205;
    public double width = 650;


    @Override
    public void start(Stage stage)  {

        try{

            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainAP.fxml"));
            Parent root = loader.load();

            controller = loader.getController();


            Scene scene = new Scene(root);
            JMetro jMetro = new JMetro();

            String css = this.getClass().getResource("main.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setTitle("Music Player");
            Image icon = new Image("icon.png");
            stage.getIcons().add(icon);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setX(200);
            stage.setY(250);
            jMetro.setScene(scene);
            jMetro.setStyle(Style.DARK);

            stage.setScene(scene);
            stage.show();

            stage.setOnCloseRequest(event -> {
                event.consume();
                logOut(stage);

            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logOut(Stage stage) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("Press OK to log out, or cancel to stay in the application.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            System.out.println("Log Out");
            controller.endTimer();
            stage.close();
        }
    }




    public static void main(String[] args) {
        launch();
    }
}