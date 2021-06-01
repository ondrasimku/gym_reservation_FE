package client.mainwindow;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import shared.Lesson;
import shared.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddLessonWindowController {
    private Stage primaryStage;

    private Socket clientSocket;
    private ObjectInputStream serverInput;
    private ObjectOutputStream clientOutput;
    private String SOCKET_HOST;
    private int SOCKET_PORT;
    private User user;

    @FXML
    private Button btnAdd, btnBack;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField fieldTime, fieldName;
    @FXML
    private TextArea fieldText;


    public void btnAddHandler(ActionEvent e) {
        if(fieldText.getText().isEmpty() || fieldTime.getText().isEmpty() || fieldName.getText().isEmpty() || datePicker.getValue() == null) {
            Alert(Alert.AlertType.ERROR, "Add status", "Add failed!", "Fields must not be empty!");
        } else {
            String inputTimeString = fieldTime.getText();
            try {
                LocalTime.parse(inputTimeString);
                if(fieldText.getLength() >= 512) {
                    Alert(Alert.AlertType.ERROR, "Add status", "Add failed!", "Text is too long!");
                }
                else {
                    LocalDate localDate = datePicker.getValue();
                    String date = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

                    String command = "add:"+date+":"+fieldTime.getText()+":"+ fieldName.getText() +":"+fieldText.getText()+":"+
                            this.user.getUsername()+":"+this.user.getPassword();
                    try {
                        clientOutput.writeObject(command);
                        clientOutput.flush();
                    } catch (IOException ioException) {
                        System.err.println("Error sending delete info, server might be down");
                        System.err.println(ioException.getMessage());
                    }

                    try {
                        String serverResponse;
                        serverResponse = (String)serverInput.readObject();
                        if(serverResponse.equals("add:failed")) {
                            Alert(Alert.AlertType.ERROR, "Add status" ,"Add failed!","Something went wrong!");
                        } else {
                            try {
                                this.user = (User) serverInput.readObject();
                                if (this.user == null) {
                                    System.err.println("Returned user is null");
                                }
                                Alert(Alert.AlertType.INFORMATION, "Add status" ,"Add successfull!","Lesson added!");
                            } catch (ClassNotFoundException classNotFoundException) {
                                Alert(Alert.AlertType.ERROR, "Add status" ,"Add failed!","Error fetching user info!");
                                System.err.println("Invalid class input exception");
                                classNotFoundException.printStackTrace();
                            }
                        }
                    } catch (IOException ioException) {
                        System.err.println("Lost connection!");
                        reconnect();
                    } catch (ClassNotFoundException classNotFoundException) {
                        System.err.println("This should not happen!");
                        classNotFoundException.printStackTrace();
                    }



                }
            } catch (DateTimeParseException | NullPointerException ex) {
                System.err.println("Invalid time string: " + inputTimeString);
            }
        }
    }

    public void btnBackHandler(ActionEvent e) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/mainwindow/LessonsWindow.fxml"));
        Parent mainScene = fxmlLoader.load();
        LessonsWindowController mainController = fxmlLoader.getController();
        mainController.init(primaryStage, clientSocket, serverInput, clientOutput, SOCKET_HOST, SOCKET_PORT, user);
        this.primaryStage.setScene(new Scene(mainScene, 600, 400));
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
        } catch(ConnectException e) {
            System.err.println("Connection failed.");
            System.err.println(e.getMessage());
        } catch(IOException e) {
            System.err.println("Exception while opening Reader and Printer in reconnect()");
            System.err.println(e.getMessage());
        }
    }

    private void Alert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
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
    }
}
