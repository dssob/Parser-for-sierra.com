package App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Sierra.com parser");
        Image icon = new Image(getClass().getResource("icon.png").toString());
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(new Scene(root, 300, 155));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
