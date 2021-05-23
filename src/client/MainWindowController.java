package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import client.User;

public class MainWindowController {

    private Socket clientSocket;
    private BufferedReader serverInput;
    private PrintWriter clientOutput;
    private ObjectInputStream serverObjectInput;
    private ObjectOutputStream clientObjectOutput;
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
            clientOutput.println(command);
            clientOutput.flush();
            System.out.println("Command send!");

            try {
                String serverResponse;
                serverResponse = serverInput.readLine();
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
                    /*try {
                        this.user = (User)serverObjectInput.readObject();
                    } catch (ClassNotFoundException classNotFoundException) {
                        System.err.println("Invalid class input exception");
                        classNotFoundException.printStackTrace();
                    }*/
                }
                System.out.println("Server says " + serverResponse);
            } catch (IOException ioException) {
                reconnect();
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
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            PrintWriter clientOutput = new PrintWriter(this.clientSocket.getOutputStream(), true);
            setSocket(this.clientSocket);
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

    public void setSocket(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.SOCKET_HOST = socket.getInetAddress().getHostAddress();
        this.SOCKET_PORT = socket.getPort();
        this.serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.clientOutput = new PrintWriter(socket.getOutputStream(), true);
        //this.serverObjectInput = new ObjectInputStream(socket.getInputStream());
       // this.clientObjectOutput = new ObjectOutputStream(socket.getOutputStream());
    }

    public void init(int port, String host) {
        this.SOCKET_PORT = port;
        this.SOCKET_HOST = host;
        this.user = null;
        reconnect();
    }

}
