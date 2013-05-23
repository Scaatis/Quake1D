package scaatis.q1d;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class QuakeServer extends Thread {
    private ServerSocket socket;
    private Quake1D q1d;
    
    public QuakeServer(Quake1D q1d) {
        this.q1d = q1d;
        try {
            socket = new ServerSocket(1996);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void run() {
        while(!socket.isClosed()) {
            try {
                Socket connection = socket.accept();
                new Thread(new Connection(connection, q1d)).start();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }
    
    public void shutdown() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
