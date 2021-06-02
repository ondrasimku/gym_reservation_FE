/**
 * This class opens login window, defines host's IP and port.
 *
 * @author Ondřej Šimků
 * */

package client.login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class LoginWindow extends Application {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 4444;

    /**
     *  This function loads LoginWindow and opens it.
     *
     * @param primaryStage Primary stage of application
     * @throws IOException Exception gets thrown if loading an object hierarchy from FXML document fails.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
        Parent loginScene = fxmlLoader.load();
        LoginWindowController controller = fxmlLoader.getController();
        primaryStage.setTitle("Lessons");
        primaryStage.setScene(new Scene(loginScene, 304, 253));
        primaryStage.getIcons().add(new Image("file:icon.png"));
        primaryStage.show();
        controller.init(SERVER_PORT, SERVER_HOST, primaryStage); // Pass PORT and IP along with primaryStage to LoginWindow's controller
    }


    public static void main(String[] args) {
        launch(args);
    }
}
