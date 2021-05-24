package client.register;

import client.login.LoginWindowController;
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

    private Socket clientSocket;
    private ObjectInputStream serverInput;
    private ObjectOutputStream clientOutput;
    private String SOCKET_HOST;
    private int SOCKET_PORT;
    private User user;


    @FXML
    private Button btnBackToLoginLogin, btnRetry, btnRegister;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword, fieldPasswordAgain;
    @FXML
    private Label labelStatus;

    public void btnBackToLoginLoginHandler(ActionEvent e) {
        try {
            openLoginWindow();
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    public void btnRegisterHandler(ActionEvent e) {

        if(fieldPassword.getText().isEmpty() || fieldUsername.getText().isEmpty() || fieldPasswordAgain.getText().isEmpty()) {
            registerAlert(Alert.AlertType.WARNING, "Register failed!","You must provide username and password!");
        } else if(!(fieldPassword.getText().equals(fieldPasswordAgain.getText()))) {
            registerAlert(Alert.AlertType.WARNING, "Register failed!","Passwords doesn't match!");
        }
        else {
            String username = fieldUsername.getText();
            String password = fieldPassword.getText();
            String passwordAgain = fieldPasswordAgain.getText();

            String command = "register:" + username + ":" + password + ":" + passwordAgain;
            System.out.println("Sending to server -->\"" + command + "\"");
            try {
                clientOutput.writeObject(command);
                clientOutput.flush();
            } catch (IOException ioException) {
                System.err.println("Error sending register info, server might be down");
                System.err.println(ioException.getMessage());
            }

            try {
                String serverResponse;
                serverResponse = (String)serverInput.readObject();
                if(serverResponse.equals("register:failed")) {
                    registerAlert(Alert.AlertType.ERROR, "Login failed!","This username already exists!");
                } else {
                    registerAlert(Alert.AlertType.INFORMATION, "Register successfull!", "Your account has been created.");
                    openLoginWindow();
                }
            } catch (IOException ioException) {
                reconnect();
            } catch (ClassNotFoundException classNotFoundException) {
                System.err.println("This should not happen!");
                classNotFoundException.printStackTrace();
            }
        }
    }

    public void btnRetryHandler(ActionEvent e) {
        reconnect();
    }

    public void setStatus(String status, Color color) {
        this.labelStatus.setText(status);
        this.labelStatus.setTextFill(color);
    }

    public void reconnect() {
        try {
            this.clientSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
            this.clientSocket.setSoTimeout(5000);
            this.clientOutput = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.clientOutput.flush();
            this.serverInput = new ObjectInputStream(this.clientSocket.getInputStream());
            this.SOCKET_HOST = this.clientSocket.getInetAddress().getHostAddress();
            this.SOCKET_PORT = this.clientSocket.getPort();
            btnRetry.setDisable(true);
            setStatus("Connected", Color.GREEN);
        } catch(ConnectException e) {
            System.err.println("Connection failed. Retrying...");
            System.err.println(e.getMessage());
            btnRetry.setDisable(false);
            setStatus("Not connected", Color.RED);
        } catch(IOException e) {
            btnRetry.setDisable(false);
            setStatus("Not connected", Color.RED);
            System.err.println("Exception while opening Reader and Printer in reconnect()");
            System.err.println(e.getMessage());
        }
    }

    public void init(Stage primaryStage,
                     Socket clientSocket,
                     ObjectInputStream serverInput,
                     ObjectOutputStream clientOutput,
                     String SOCKET_HOST,
                     int SOCKET_PORT,
                     User user) {
        this.primaryStage = primaryStage;
        this.clientSocket = clientSocket;
        this.serverInput = serverInput;
        this.clientOutput = clientOutput;
        this.SOCKET_HOST = SOCKET_HOST;
        this.SOCKET_PORT = SOCKET_PORT;
        this.user = user;
        reconnect();
    }

    public void openLoginWindow() throws IOException, ClassNotFoundException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/login/LoginWindow.fxml"));
        Parent mainScene = fxmlLoader.load();
        LoginWindowController mainController = fxmlLoader.getController();
        mainController.initAfterRegister(primaryStage, clientSocket, serverInput, clientOutput, SOCKET_HOST, SOCKET_PORT, user);
        this.primaryStage.setScene(new Scene(mainScene, 304, 253));
    }

    private void registerAlert(Alert.AlertType type,String header,String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Register status");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }
}
