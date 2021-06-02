package client.register;

import client.Debug;
import client.login.LoginWindowController;
import client.socketmanager.SocketManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import shared.User;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class RegisterWindowController {
    private Stage primaryStage;
    private SocketManager socketManager;
    private User user;


    @FXML
    private Button btnBackToLoginLogin, btnRetry, btnRegister;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword, fieldPasswordAgain;
    @FXML
    private Label labelStatus;

    public void btnRegisterHandler(ActionEvent e) {
        if(fieldPassword.getText().isEmpty() || fieldUsername.getText().isEmpty() || fieldPasswordAgain.getText().isEmpty()) {
            Alert(Alert.AlertType.WARNING, "Register status","Register failed!","You must provide username and password!");
        } else if(!(fieldPassword.getText().equals(fieldPasswordAgain.getText()))) {
            Alert(Alert.AlertType.WARNING, "Register status", "Register failed!","Passwords doesn't match!");
        }
        else {
            String username = fieldUsername.getText();
            String password = fieldPassword.getText();
            String passwordAgain = fieldPasswordAgain.getText();
            String command = "register:" + username + ":" + password + ":" + passwordAgain;

            try {
                socketManager.clientOutput.writeObject(command);
                socketManager.clientOutput.flush();
                String serverResponse;
                serverResponse = (String)socketManager.serverInput.readObject();

                if(serverResponse.equals("register:failed")) {
                    Alert(Alert.AlertType.ERROR, "Register status", "Login failed!","This username already exists!");
                } else {
                    Alert(Alert.AlertType.ERROR, "Register status", "Register successfull!", "Your account has been created.");
                    openLoginWindow();
                }
            } catch (IOException ioException) {
                if(Debug.debug_mode) {
                    System.err.println("Error sending register info, server might be down");
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

    public void btnBackToLoginLoginHandler(ActionEvent e) {
        openLoginWindow();
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

    public void init(Stage primaryStage, SocketManager socketManager, User user) {
        this.primaryStage = primaryStage;
        this.socketManager = socketManager;
        this.user = user;
        reconnect();
    }

    public void setStatus(String status, Color color) {
        labelStatus.setText(status);
        labelStatus.setTextFill(color);
    }

    private void Alert(Alert.AlertType type, String title,String header,String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    public void openLoginWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/login/LoginWindow.fxml"));
            Parent mainScene = fxmlLoader.load();
            LoginWindowController mainController = fxmlLoader.getController();
            mainController.init(primaryStage, socketManager, user);
            this.primaryStage.setScene(new Scene(mainScene, 304, 253));
        } catch(IOException ioException) {
            if(Debug.debug_mode)
            {
                ioException.printStackTrace();
            }
        }
    }

}
