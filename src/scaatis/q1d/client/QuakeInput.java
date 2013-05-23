package scaatis.q1d.client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;

import scaatis.q1d.PlayerAction;

public class QuakeInput extends JFrame {

    private static final long serialVersionUID = 1L;
    private Socket            connection;
    private boolean           running;
    private PlayerAction      action;

    public QuakeInput(String host) {
        super("input");
        setSize(100, 100);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
                key(arg0);
            }
        });
        try {
            connection = new Socket(host, 1996);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        action = PlayerAction.IDLE;
        running = false;
    }

    public void start() {
        running = true;
        setVisible(true);
        PrintWriter writer = null;
        BufferedReader input = null;
        try {
            writer = new PrintWriter(connection.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            stop();
        }
        
        writer.println("{\"message\":\"connect\",\"color\":" + 0xff0000 + "}");
        
        while(running) {
            try {
                input.readLine();
            } catch (IOException e) {
                stop();
                break;
            }
            writer.println("{\"message\":\"action\",\"type\":\"" + action.name() + "\"}");
            action = PlayerAction.IDLE;
        }

        try {
            if (writer != null) {
                writer.close();
            }
            if (input != null) {
                input.close();
            }
            connection.close();
        } catch (IOException e) {

        }
        dispose();
    }

    public void stop() {
        running = false;
    }

    public void key(KeyEvent evt) {
        switch(evt.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_RIGHT:
            action = PlayerAction.TURN;
            break;
        case KeyEvent.VK_UP:
            action = PlayerAction.JUMP;
            break;
        case KeyEvent.VK_SPACE:
            action = PlayerAction.SHOOT;
            break;
        case KeyEvent.VK_M:
            action = PlayerAction.MOVE;
            break;
        default:
            action = PlayerAction.IDLE;
            break;
        }
    }
    
    public static void main(String[] args) {
        QuakeInput input = new QuakeInput("localhost");
        input.start();
    }
}
