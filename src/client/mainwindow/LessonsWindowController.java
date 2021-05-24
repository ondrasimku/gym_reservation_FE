package client.mainwindow;

import javafx.collections.FXCollections;
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
import java.util.Optional;

public class LessonsWindowController {
    private Stage primaryStage;

    private Socket clientSocket;
    private ObjectInputStream serverInput;
    private ObjectOutputStream clientOutput;
    private String SOCKET_HOST;
    private int SOCKET_PORT;
    private User user;
    private ObservableList<Lesson> list;

    @FXML
    private Button btnBack, btnBookIn, btnDelete;
    @FXML
    private TableView<Lesson> tableViewLessons;
    @FXML
    private TableColumn clnDate, clnName, clnText;

    public void btnBookInHandler(ActionEvent e) {
        Lesson selectedLesson = tableViewLessons.getSelectionModel().getSelectedItem();
        if(selectedLesson == null) {
            Alert(Alert.AlertType.ERROR, "Book in status" ,"Book in failed!","Select lesson!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Book in lesson");
        alert.setContentText("Are sure you want to book this lesson?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK) {

            String command = "bookin:" + selectedLesson.getID() + ":" + this.user.getId_user()
                    + ":" + this.user.getUsername() + ":" + this.user.getPassword();
            System.out.println("Sending to server -->\"" + command + "\"");
            try {
                clientOutput.writeObject(command);
                clientOutput.flush();
            } catch (IOException ioException) {
                System.err.println("Error sending bookin info, server might be down");
                System.err.println(ioException.getMessage());
            }

            try {
                String serverResponse;
                serverResponse = (String)serverInput.readObject();
                if(serverResponse.equals("bookin:failed")) {
                    Alert(Alert.AlertType.ERROR, "Book in status" ,"Book in failed!","Something went wrong!");
                } else {
                    Alert(Alert.AlertType.INFORMATION, "Book in status" ,"Book in successfull!","You got booked in!");
                    try {
                        this.user = (User)serverInput.readObject();
                        this.list.removeAll();
                        this.list = getLessons();
                        this.tableViewLessons.setItems(this.list);
                    } catch (ClassNotFoundException classNotFoundException) {
                        Alert(Alert.AlertType.ERROR, "Book in status" ,"Book in failed!","Error fetching user info!");
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
    public void btnBackHandler(ActionEvent e) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/mainwindow/MainWindow.fxml"));
        Parent mainScene = fxmlLoader.load();
        MainWindowController mainController = fxmlLoader.getController();
        mainController.init(primaryStage, clientSocket, serverInput, clientOutput, SOCKET_HOST, SOCKET_PORT, user);
        this.primaryStage.setScene(new Scene(mainScene, 600, 400));
    }

    public void btnDeleteHandler(ActionEvent e) {
        Lesson selectedLesson = tableViewLessons.getSelectionModel().getSelectedItem();
        if(selectedLesson == null) {
            Alert(Alert.AlertType.ERROR, "Delete status" ,"Delete failed!","Select lesson!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Delete lesson");
        alert.setContentText("Are sure you want to delete this lesson?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK) {

            String command = "delete:" + selectedLesson.getID() + ":" + this.user.getId_user()
                    + ":" + this.user.getUsername() + ":" + this.user.getPassword();
            System.out.println("Sending to server -->\"" + command + "\"");
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
                if(serverResponse.equals("delete:failed")) {
                    Alert(Alert.AlertType.ERROR, "Delete status" ,"Delete failed!","Something went wrong!");
                } else {
                    Alert(Alert.AlertType.INFORMATION, "Delete status" ,"Delete successfull!","Lesson deleted!");
                    try {
                        this.user = (User)serverInput.readObject();
                        this.list.removeAll();
                        this.list = getLessons();
                        this.tableViewLessons.setItems(this.list);
                    } catch (ClassNotFoundException classNotFoundException) {
                        Alert(Alert.AlertType.ERROR, "Delete status" ,"Delete failed!","Error fetching user info!");
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
        ObservableList<Lesson> lessons = FXCollections.observableArrayList(user.getUpcomingLessons());
        return lessons;
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
        if(this.user.getIs_instructor() == 1) {
            btnDelete.setVisible(true);
        } else {
            btnDelete.setVisible(false);
        }
        this.clnDate.setCellValueFactory(new PropertyValueFactory<Lesson, String>("date"));
        this.clnName.setCellValueFactory(new PropertyValueFactory<Lesson, String>("name"));
        this.clnText.setCellValueFactory(new PropertyValueFactory<Lesson, String>("text"));
        if(this.list != null) {
            this.list.removeAll();
        }
        this.list = getLessons();
        this.tableViewLessons.setItems(this.list);
    }
}
