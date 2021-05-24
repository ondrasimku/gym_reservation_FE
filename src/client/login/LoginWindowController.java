package client.login;

import client.register.RegisterWindowController;
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

public class LoginWindowController {

    private Stage primaryStage;

    private Socket clientSocket;
    private ObjectInputStream serverInput;
    private ObjectOutputStream clientOutput;
    private String SOCKET_HOST;
    private int SOCKET_PORT;
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
            loginAlert(Alert.AlertType.WARNING, "Login failed!","You must provide username and password!");
        } else {
            String username = fieldUsername.getText();
            String password = fieldPassword.getText();

            String command = "login:" + username + ":" + password;
            System.out.println("Sending to server -->\"" + command + "\"");
            try {
                clientOutput.writeObject(command);
                clientOutput.flush();
            } catch (IOException ioException) {
                System.err.println("Error sending login info, server might be down");
                System.err.println(ioException.getMessage());
            }
            System.out.println("Command send!");

            try {
                String serverResponse;
                serverResponse = (String)serverInput.readObject();
                if(serverResponse.equals("login:failed")) {
                    loginAlert(Alert.AlertType.ERROR, "Login failed!","Wrong username or password!");
                } else {
                    // Read User object with his lessons
                    try {
                        this.user = (User)serverInput.readObject();
                        openMainWindow();
                    } catch (ClassNotFoundException classNotFoundException) {
                        loginAlert(Alert.AlertType.ERROR, "Login failed!","Error fetching user info from server");
                        System.err.println("Invalid class input exception");
                        classNotFoundException.printStackTrace();
                    }
                }
                System.out.println("Server says " + serverResponse);
            } catch (IOException ioException) {
                reconnect();
            } catch (ClassNotFoundException classNotFoundException) {
                System.err.println("This should not happen!");
                classNotFoundException.printStackTrace();
            }
        }

    }

    public void btnRegisterHandler(ActionEvent e) {
        try {
            openRegisterWindow();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
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

    public void init(int port, String host, Stage primaryStage) {
        this.SOCKET_PORT = port;
        this.SOCKET_HOST = host;
        this.user = null;
        this.primaryStage = primaryStage;
        reconnect();
    }

    public void initAfterRegister(Stage primaryStage,
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

    private void loginAlert(Alert.AlertType type,String header,String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Login status");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    private void openMainWindow() throws IOException, ClassNotFoundException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/mainwindow/MainWindow.fxml"));
        Parent mainScene = fxmlLoader.load();
        MainWindowController mainController = fxmlLoader.getController();
        mainController.init(primaryStage, clientSocket, serverInput, clientOutput, SOCKET_HOST, SOCKET_PORT, user);
        this.primaryStage.close();
        this.primaryStage.setScene(new Scene(mainScene, 600, 400));
        this.primaryStage.show();
    }

    private void openRegisterWindow() throws IOException, ClassNotFoundException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/register/RegisterWindow.fxml"));
        Parent mainScene = fxmlLoader.load();
        RegisterWindowController mainController = fxmlLoader.getController();
        mainController.init(primaryStage, clientSocket, serverInput, clientOutput, SOCKET_HOST, SOCKET_PORT, user);
        this.primaryStage.setScene(new Scene(mainScene, 304, 305));
    }

}
