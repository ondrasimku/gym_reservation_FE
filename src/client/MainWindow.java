package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.Buffer;


public class MainWindow extends Application {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 4444;
    private static boolean connected = false;
    private Socket socket;
    @Override
    public void start(Stage primaryStage) throws Exception{


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent loginScene = fxmlLoader.load();
        MainWindowController controller = fxmlLoader.getController();
        primaryStage.setTitle("Lessons");
        primaryStage.setScene(new Scene(loginScene, 300, 275));
        primaryStage.show();
        controller.init(SERVER_PORT, SERVER_HOST);
        /*while(!connected) {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                socket.setSoTimeout(5000);
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter clientOutput = new PrintWriter(socket.getOutputStream(), true);
                controller.setSocket(socket);
                controller.setStatus("Connected", Color.GREEN);
                connected = true;
            } catch(ConnectException e) {
                System.out.println("Connection failed. Retrying...");
                controller.setStatus("Connecting", Color.RED);
                try { Thread.sleep(2000); }
                catch (InterruptedException ie) { ie.printStackTrace(); }
            }
        }*/
    }


    public static void main(String[] args) {
        launch(args);
    }
}
