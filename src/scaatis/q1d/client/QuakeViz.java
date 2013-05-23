package scaatis.q1d.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuakeViz extends JFrame {

    public static final int   windowWidth      = 1024;
    public static final int   windowHeight     = 300;

    private static final long serialVersionUID = 1L;
    private Socket            connection;
    private boolean           running;
    private Font              font;

    public QuakeViz(String adress) {
        super("Quake 1D");
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(windowWidth, windowHeight);
        setIgnoreRepaint(true);
        font = new Font("Monospaced", Font.BOLD, 12);
        running = false;
        try {
            connection = new Socket(adress, 1996);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        running = true;
        setVisible(true);
        createBufferStrategy(2);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
            out.println("{\"message\":\"connect\"}");
        } catch (IOException e) {
            stop();
        }
        while (running) {
            String line;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                stop();
                break;
            }
            if (line == null) {
                stop();
                break;
            }

            JSONObject object;
            try {
                object = new JSONObject(line);
            } catch (JSONException e) {
                System.out.println(line);
                stop();
                break;
            }
            String message = object.optString("message", "");
            if (message.equals("gamestate")) {
                drawGameState(object);
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            // meh
        }

        setVisible(false);
        dispose();
    }

    private void drawGameState(JSONObject state) {
        Graphics graphics = getBufferStrategy().getDrawGraphics();
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, windowWidth, windowHeight);
        int tileWidth = windowWidth / state.getInt("width");
        int y = (windowHeight - tileWidth) / 2;
        graphics.setColor(new Color(0x444444));
        graphics.fillRect(0, y, windowWidth, tileWidth);
        int offset = state.getInt("offset");
        JSONArray array = state.getJSONArray("items");
        HashMap<Integer, Integer> players = new HashMap<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject current = array.getJSONObject(i);
            if (current.getString("message").equals("player")) {
                players.put(current.getInt("color"), current.getInt("score"));
                graphics.setColor(new Color(current.getInt("color")));
                int x = (current.getInt("x") + offset) * tileWidth;
                graphics.fillRect(x, y, tileWidth, tileWidth);
            } else if (current.getString("message").equals("bullet")) {
                graphics.setColor(new Color(current.getInt("shooter")));
                int x = (int) ((current.getInt("x") + offset + 0.375) * tileWidth);
                int y2 = y + (int) (0.375 * tileWidth);
                graphics.fillRect(x, y2, tileWidth / 4, tileWidth / 4);
            }
        }
        int scoreW = windowWidth / 4;
        int i = 0;
        graphics.setFont(font);
        for (Integer player : players.keySet()) {
            graphics.setColor(new Color(player));
            int x = (i % 4) * scoreW;
            int y2 = i < 4 ? 0 : windowHeight - 64;
            graphics.fillRect(x, y2, 64, 64);
            graphics.drawString(players.get(player).toString(), x + 68, y2);
            i++;
        }
        getBufferStrategy().show();
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        QuakeViz viz = new QuakeViz("localhost");
        viz.start();
    }
}
