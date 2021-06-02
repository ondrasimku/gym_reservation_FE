package client.login;

import client.Debug;
import client.register.RegisterWindowController;
import client.socketmanager.SocketManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import javafx.stage.Stage;
import shared.User;
import client.mainwindow.MainWindowController;


/**
 * LoginWindowController handles all events in LoginWindow and opens Socket connection
 * to server using reconnect method.
 *
 */
public class LoginWindowController {

    private Stage primaryStage;
    private SocketManager socketManager;
    private User user;

    @FXML
    private Button btnLogin, btnRetry, btnRegister;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Label labelStatus;

    public void btnLoginHandler(ActionEvent e) {

        if(fieldPassword.getText().isEmpty() || fieldUsername.getText().isEmpty()) {
            Alert(Alert.AlertType.WARNING, "Login status" ,"Login failed!","You must provide username and password!");
        } else {
            String username = fieldUsername.getText();
            String password = fieldPassword.getText();
            String command = "login:" + username + ":" + password;

            try {
                socketManager.clientOutput.writeObject(command);
                socketManager.clientOutput.flush();
                String serverResponse;
                serverResponse = (String)socketManager.serverInput.readObject();

                if(serverResponse.equals("login:failed")) {
                    Alert(Alert.AlertType.ERROR, "Login status" , "Login failed!","Wrong username or password!");
                } else {
                    // Read User object with his lessons
                    this.user = (User)socketManager.serverInput.readObject();
                    openMainWindow();
                }
            } catch (IOException ioException) {
                if(Debug.debug_mode) {
                    System.err.println("Error sending login info, server might be down");
                    System.err.println(ioException.getMessage());
                }
                reconnect();
            } catch (ClassNotFoundException classNotFoundException) {
                if(Debug.debug_mode) {
                    System.err.println("Trying to get non-existent class!");
                    classNotFoundException.printStackTrace();
                }
            }
        }

    }

    public void btnRegisterHandler(ActionEvent e) {
        openRegisterWindow();
    }

    public void btnRetryHandler(ActionEvent e) {
        reconnect();
    }

    public void reconnect() {
        try {
            socketManager.reconnect();
            btnRetry.setDisable(true);
            setStatus("Connected", Color.GREEN);
        } catch(ConnectException e) {
            if(Debug.debug_mode) {
                System.err.println("Connection failed.");
                System.err.println(e.getMessage());
            }
            btnRetry.setDisable(false);
            setStatus("Not connected", Color.RED);
        } catch(IOException e) {
            if(Debug.debug_mode) {
                System.err.println("Exception while opening Reader and Printer in reconnect()");
                System.err.println(e.getMessage());
            }
            btnRetry.setDisable(false);
            setStatus("Not connected", Color.RED);
        }
    }

    public void init(int port, String host, Stage primaryStage) {
        this.socketManager = new SocketManager(host, port);
        this.user = null;
        this.primaryStage = primaryStage;
        reconnect();
    }

    public void init(Stage primaryStage, SocketManager socketManager, User user) {
        this.socketManager = socketManager;
        this.user = user;
        this.primaryStage = primaryStage;
        reconnect();
    }

    public void setStatus(String status, Color color) {
        labelStatus.setText(status);
        labelStatus.setTextFill(color);
    }

    private void Alert(Alert.AlertType type, String title ,String header,String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    private void openMainWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/mainwindow/MainWindow.fxml"));
            Parent mainScene = fxmlLoader.load();
            MainWindowController mainController = fxmlLoader.getController();
            mainController.init(primaryStage, socketManager, user);
            this.primaryStage.close();
            this.primaryStage.setScene(new Scene(mainScene, 600, 400));
            this.primaryStage.show();
        } catch(IOException ioException) {
            if(Debug.debug_mode)
            {
                ioException.printStackTrace();
            }
        }
    }

    private void openRegisterWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/register/RegisterWindow.fxml"));
            Parent mainScene = fxmlLoader.load();
            RegisterWindowController mainController = fxmlLoader.getController();
            mainController.init(primaryStage, socketManager, user);
            this.primaryStage.setScene(new Scene(mainScene, 304, 305));
        } catch(IOException ioException) {
            if(Debug.debug_mode)
            {
                ioException.printStackTrace();
            }
        }
    }

}
