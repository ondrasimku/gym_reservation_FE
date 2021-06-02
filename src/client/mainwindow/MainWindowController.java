package client.mainwindow;

import client.Debug;
import client.register.RegisterWindowController;
import client.socketmanager.SocketManager;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import shared.Lesson;
import shared.User;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Optional;

public class MainWindowController {

    private Stage primaryStage;
    private SocketManager socketManager;
    private User user;
    private ObservableList<Lesson> list;

    @FXML
    private Button btnLessonsList, btnBookOut;
    @FXML
    private TableView<Lesson> tableViewLessons;
    @FXML
    private TableColumn clnDate, clnName, clnText;

    public void btnBookOutHandler(ActionEvent e) {
        Lesson selectedLesson = tableViewLessons.getSelectionModel().getSelectedItem();
        if(selectedLesson == null) {
            Alert(Alert.AlertType.ERROR, "Bookout status" ,"Bookout failed!","Select lesson!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Book out lesson");
        alert.setContentText("Are sure you want to to book out this lesson?");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.get() == ButtonType.OK) {
            String command = "bookout:" + selectedLesson.getID() + ":" + this.user.getId_user()
                    + ":" + this.user.getUsername() + ":" + this.user.getPassword();

            try {
                socketManager.clientOutput.writeObject(command);
                socketManager.clientOutput.flush();
                String serverResponse;
                serverResponse = (String)socketManager.serverInput.readObject();

                if(serverResponse.equals("bookout:failed")) {
                    Alert(Alert.AlertType.ERROR, "Bookout status" ,"Bookout failed!","Something went wrong!");
                } else {
                    Alert(Alert.AlertType.INFORMATION, "Bookout status" ,"Bookout successfull!","You got booked out!");
                    this.user = (User)socketManager.serverInput.readObject();
                    this.list.removeAll();
                    this.list = getLessons();
                    this.tableViewLessons.setItems(this.list);
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

    public void btnLessonsListHandler(ActionEvent e) {
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
        this.clnDate.setCellValueFactory(new PropertyValueFactory<Lesson, String>("date"));
        this.clnName.setCellValueFactory(new PropertyValueFactory<Lesson, String>("name"));
        this.clnText.setCellValueFactory(new PropertyValueFactory<Lesson, String>("text"));
        if(this.list != null) {
            this.list.removeAll();
        }
        this.list = getLessons();
        this.tableViewLessons.setItems(this.list);
    }

    private ObservableList<Lesson> getLessons() {
        ObservableList<Lesson> lessons = FXCollections.observableArrayList(user.getLessons());
        return lessons;
    }

    private void Alert(Alert.AlertType type, String title,String header,String message) {
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
