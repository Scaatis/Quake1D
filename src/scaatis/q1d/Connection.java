package scaatis.q1d;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connection implements Runnable, Closeable {
    private Socket                        socket;
    private PrintWriter                   out;
    private BufferedReader                in;
    private ConcurrentLinkedQueue<String> queuedInput;
    private Player                        player;
    private Quake1D                       protocol;

    public Connection(Socket socket, Quake1D protocol) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.protocol = protocol;
    }

    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }

    @Override
    public void run() {
        boolean running = true;
        String line;
        try {
            line = in.readLine();
            player = protocol.attemptHandshake(line);
            if (player == null) {
                running = false;
                close();
            }
        } catch (IOException e) {
            try {
                close();
            } catch (IOException e2) {
            }
            return;
        }
        protocol.connectNew(this);
        while (running && !socket.isClosed()) {
            try {
                line = in.readLine();
                queuedInput.add(line);
            } catch (IOException e) {
                try {
                    close();
                } catch (IOException e2) {
                    // shit's fucked man
                }
                running = false;
            }
        }
    }

    public void send(String message) {
        if (socket.isClosed()) {
            return;
        }
        out.println(message);
    }

    public boolean hasInput() {
        return !queuedInput.isEmpty();
    }

    public String nextLine() {
        return queuedInput.poll();
    }

    public Player getPlayer() {
        return player;
    }
    
    public boolean isClosed() {
        return socket.isClosed();
    }
    
    public void clearInput() {
        queuedInput.clear();
    }
}
