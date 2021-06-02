package client.socketmanager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketManager {
    public Socket clientSocket;
    public ObjectInputStream serverInput;
    public ObjectOutputStream clientOutput;
    public String SOCKET_HOST;
    public int SOCKET_PORT;

    public SocketManager(
                         String SOCKET_HOST,
                         int SOCKET_PORT) {
        this.SOCKET_HOST = SOCKET_HOST;
        this.SOCKET_PORT = SOCKET_PORT;
    }

    public void reconnect() throws IOException {
        this.clientSocket = new Socket(SOCKET_HOST, SOCKET_PORT);
        this.clientSocket.setSoTimeout(5000);
        this.clientOutput = new ObjectOutputStream(this.clientSocket.getOutputStream());
        this.clientOutput.flush();
        this.serverInput = new ObjectInputStream(this.clientSocket.getInputStream());
        this.SOCKET_HOST = this.clientSocket.getInetAddress().getHostAddress();
        this.SOCKET_PORT = this.clientSocket.getPort();
    }
}
