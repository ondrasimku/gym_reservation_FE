package client.mainwindow;

import client.register.RegisterWindowController;
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

    private Socket clientSocket;
    private ObjectInputStream serverInput;
    private ObjectOutputStream clientOutput;
    private String SOCKET_HOST;
    private int SOCKET_PORT;
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
            System.out.println("Sending to server -->\"" + command + "\"");
            try {
                clientOutput.writeObject(command);
                clientOutput.flush();
            } catch (IOException ioException) {
                System.err.println("Error sending bookout info, server might be down");
                System.err.println(ioException.getMessage());
            }

            try {
                String serverResponse;
                serverResponse = (String)serverInput.readObject();
                if(serverResponse.equals("bookout:failed")) {
                    Alert(Alert.AlertType.ERROR, "Bookout status" ,"Bookout failed!","Something went wrong!");
                } else {
                    Alert(Alert.AlertType.INFORMATION, "Bookout status" ,"Bookout successfull!","You got booked out!");
                    try {
                        this.user = (User)serverInput.readObject();
                        this.list.removeAll();
                        this.list = getLessons();
                        this.tableViewLessons.setItems(this.list);
                    } catch (ClassNotFoundException classNotFoundException) {
                        Alert(Alert.AlertType.ERROR, "Bookout status" ,"Bookout failed!","Error fetching user info!");
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
    }

    private ObservableList<Lesson> getLessons() {
        ObservableList<Lesson> lessons = FXCollections.observableArrayList(user.getLessons());
        return lessons;
    }

    public void btnLessonsListHandler(ActionEvent e) {
        try {
            openLessonsWindow();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
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
        this.clnDate.setCellValueFactory(new PropertyValueFactory<Lesson, String>("date"));
        this.clnName.setCellValueFactory(new PropertyValueFactory<Lesson, String>("name"));
        this.clnText.setCellValueFactory(new PropertyValueFactory<Lesson, String>("text"));
        if(this.list != null) {
            this.list.removeAll();
        }
        this.list = getLessons();
        this.tableViewLessons.setItems(this.list);
    }

    private void Alert(Alert.AlertType type, String title,String header,String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
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

    private void openLessonsWindow() throws IOException, ClassNotFoundException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/mainwindow/LessonsWindow.fxml"));
        Parent mainScene = fxmlLoader.load();
        LessonsWindowController mainController = fxmlLoader.getController();
        mainController.init(primaryStage, clientSocket, serverInput, clientOutput, SOCKET_HOST, SOCKET_PORT, user);
        this.primaryStage.setScene(new Scene(mainScene, 600, 400));
    }
}
