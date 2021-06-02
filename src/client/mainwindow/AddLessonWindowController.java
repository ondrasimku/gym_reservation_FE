package client.mainwindow;

import client.Debug;
import client.socketmanager.SocketManager;
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
    private SocketManager socketManager;
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
            } catch (DateTimeParseException | NullPointerException ex) {
                if(Debug.debug_mode) {
                    System.err.println("Invalid time string: " + inputTimeString);
                }
            }

            if(fieldText.getLength() >= 512) {
                Alert(Alert.AlertType.ERROR, "Add status", "Add failed!", "Text is too long!");
            }
            else {
                LocalDate localDate = datePicker.getValue();
                String date = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                String command = "add:"+date+":"+fieldTime.getText()+":"+ fieldName.getText() +":"+fieldText.getText()+":"+
                        this.user.getUsername()+":"+this.user.getPassword();

                try {
                    socketManager.clientOutput.writeObject(command);
                    socketManager.clientOutput.flush();
                    String serverResponse;
                    serverResponse = (String)socketManager.serverInput.readObject();

                    if(serverResponse.equals("add:failed")) {
                        Alert(Alert.AlertType.ERROR, "Add status" ,"Add failed!","Something went wrong!");
                    } else {
                        this.user = (User)socketManager.serverInput.readObject();
                        Alert(Alert.AlertType.INFORMATION, "Add status" ,"Add successfull!","Lesson added!");
                    }

                } catch (IOException ioException) {
                    if(Debug.debug_mode) {
                        System.err.println("Error sending bookout info, server might be down");
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

    }

    public void btnBackHandler(ActionEvent e) {
        openLessonsWindow();
    }

    public void reconnect() {
        try {
            socketManager.reconnect();
        } catch(ConnectException e) {
            if(Debug.debug_mode) {
                System.err.println("Connection failed.");
                System.err.println(e.getMessage());
            }
        } catch(IOException e) {
            if(Debug.debug_mode) {
                System.err.println("Exception while opening Reader and Printer in reconnect()");
                System.err.println(e.getMessage());
            }
        }
    }

    public void init(Stage primaryStage, SocketManager socketManager, User user) {
        this.primaryStage = primaryStage;
        this.socketManager = socketManager;
        this.user = user;
    }

    private void Alert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    private void openLessonsWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/mainwindow/LessonsWindow.fxml"));
            Parent mainScene = fxmlLoader.load();
            LessonsWindowController mainController = fxmlLoader.getController();
            mainController.init(primaryStage, socketManager, user);
            this.primaryStage.setScene(new Scene(mainScene, 600, 400));
        } catch (IOException ioException) {
            if(Debug.debug_mode)
            {
                ioException.printStackTrace();
            }
        }
    }

}
