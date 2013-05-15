package scaatis.q1d;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

public class Connection implements Runnable, Closeable {
    public static final JSONObject        successfulHandshake = new JSONObject();
    static {
        successfulHandshake.put("message", "connect");
        successfulHandshake.put("status", true);
    }

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
    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Connection to " + socket.getInetAddress().toString() + " could not be closed properly.");
        }
    }

    @Override
    public void run() {
        boolean running = true;
        String line;
        try {
            socket.setSoTimeout(10000);
            line = in.readLine();
            player = protocol.attemptHandshake(line);
            socket.setSoTimeout(0);
            if (player == null) {
                close();
                return;
            }
        } catch (SocketTimeoutException timeout) {
            System.out.println(socket.getInetAddress().toString() + " did not send handshake.");
            close();
            return;
        } catch (IOException e) {
            close();
            return;
        }
        protocol.connectNew(this);
        send(successfulHandshake.toString());
        while (running && !socket.isClosed()) {
            try {
                line = in.readLine();
                queuedInput.add(line);
            } catch (IOException e) {
                close();
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
