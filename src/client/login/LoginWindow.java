package client.login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.Socket;

/*
* MAIN CLASS OF APPLICATION
*
* This class opens login window, defines
*
* */
public class LoginWindow extends Application {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 4444;
    private Socket socket;
    @Override
    public void start(Stage primaryStage) throws Exception{


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
        Parent loginScene = fxmlLoader.load();
        LoginWindowController controller = fxmlLoader.getController();
        primaryStage.setTitle("Lessons");
        primaryStage.setScene(new Scene(loginScene, 304, 253));
        primaryStage.getIcons().add(new Image("file:icon.png"));
        primaryStage.show();
        controller.init(SERVER_PORT, SERVER_HOST, primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
