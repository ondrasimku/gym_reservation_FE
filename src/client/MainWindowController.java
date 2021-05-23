package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import shared.User;

public class MainWindowController {

    private Socket clientSocket;
    private ObjectInputStream serverInput;
    private ObjectOutputStream clientOutput;
    private String SOCKET_HOST;
    private int SOCKET_PORT;
    private User user;


    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRetry;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Label labelStatus;

    public void btnLoginHandler(ActionEvent e) {

        if(fieldPassword.getText().isEmpty() || fieldUsername.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Login status");
            alert.setHeaderText("Login failed!");
            alert.setContentText("You must provide username and password!");
            alert.show();
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
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Login status");
                    alert.setHeaderText("Login failed!");
                    alert.setContentText("Wrong username or password!");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Login status");
                    alert.setHeaderText("Login successfull!");
                    alert.setContentText("Switch windows!");
                    alert.show();
                    // Read User object with his lessons
                    try {
                        this.user = (User)serverInput.readObject();
                        System.out.println(this.user.toString());
                    } catch (ClassNotFoundException classNotFoundException) {
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

    public void init(int port, String host) {
        this.SOCKET_PORT = port;
        this.SOCKET_HOST = host;
        this.user = null;
        reconnect();
    }

}
