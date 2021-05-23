package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainWindowController {

    private Socket clientSocket;
    private BufferedReader serverInput;
    private PrintWriter clientOutput;

    @FXML
    private Button btnLogin;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;

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

            String command = "login:"+username+":"+password;
            System.out.println("Sending to server -->\"" + command + "\"");
            clientOutput.println(command);
            clientOutput.flush();
            System.out.println("Command send!");
            try {
                String serverResponse;
                while((serverResponse = serverInput.readLine()) != null) {
                    System.out.println("Server says " + serverResponse);
                    break;
                }
            } catch (IOException ioException) {
                System.out.println("Error!");
                ioException.printStackTrace();
            }
        }

    }

    public void setSocket(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.clientOutput = new PrintWriter(socket.getOutputStream(), true);
    }
}
