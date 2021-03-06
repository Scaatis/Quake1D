package scaatis.q1d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Quake1D {

    public static final int                   similarityThreshold = 0x100;
    public static final int                   bgColor             = 0x444444;
    public static final int                   turnsPerSecond      = 1;
    public static final int                   turnsPerRound       = 100;
    public static final int                   pauseBetweenRounds  = 5;
    public static final int                   fps                 = 20;

    private Collection<Connection>            connections;
    private ConcurrentLinkedQueue<Connection> newConnections;
    private Arena                             arena;
    private double                            timeSinceLastTurn;
    private int                               turns;
    private double                            pause;
    private QuakeControls                     controls;
    private boolean                           running;

    public Quake1D() {
        connections = new TreeSet<>(new Comparator<Connection>() {
            @Override
            public int compare(Connection arg0, Connection arg1) {
                return Integer.compare(arg0.getPlayer().getColor(),
                        arg1.getPlayer().getColor());
            }
        });
        newConnections = new ConcurrentLinkedQueue<>();
        arena = new Arena();
        timeSinceLastTurn = 0;
        turns = 0;
        pause = 0;
        controls = new QuakeControls();
        controls.getBtnKick().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                controls.getSelected().close();
            }
        });
        controls.getBtnRestartRound().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turns = turnsPerRound - 1;
            }
        });
        running = false;
    }

    public Player attemptHandshake(String handshake) {
        JSONObject message;
        try {
            message = new JSONObject(handshake);
        } catch (JSONException e) {
            return null;
        }

        if (!message.optString("message", "").equals("connect")) {
            return null;
        }

        int color = message.optInt("color", -1);
        if (!checkColor(color)) {
            return null;
        }
        return new Player(color);
    }

    private boolean checkColor(int color) {
        if (color == -1) {
            return true;
        }
        if (color > 0xFFFFFF || color <= 0) {
            return false;
        }
        if (colorDistanceSq(color, bgColor) < similarityThreshold) {
            return false;
        }
        for (Player player : getPlayers()) {
            if (colorDistanceSq(color, player.getColor()) < similarityThreshold) {
                return false;
            }
        }
        return true;
    }

    public Collection<Player> getPlayers() {
        Collection<Player> players = new ArrayList<>();
        for (Connection conn : getConnections()) {
            players.add(conn.getPlayer());
        }
        return players;
    }

    public Collection<Connection> getConnections() {
        return new ArrayList<Connection>(connections);
    }

    public static double colorDistanceSq(int colorA, int colorB) {
        int diff = colorB - colorA;
        int r = diff >> 16;
        int g = (diff >> 8) & 0xFF;
        int b = diff & 0xFF;
        return r * r + g * g + b * b;
    }

    public void update(double delta) {
        if (pause > 0) {
            pause -= delta;
            if (pause <= 0) {
                arena = new Arena();
                turns = 0;
            } else {
                return;
            }
        }

        while (!newConnections.isEmpty()) {
            Connection conn = newConnections.poll();
            if (!conn.getPlayer().isObserver()) {
                arena.newPlayer(conn.getPlayer());
            }
            System.out.println(conn.getPlayer().toString() + " has joined.");
            connections.add(conn);
            controls.addElement(conn);
        }

        Iterator<Connection> iter = connections.iterator();
        while (iter.hasNext()) {
            Connection current = iter.next();
            if (current.isClosed()) {
                arena.playerLeft(current.getPlayer());
                iter.remove();
                controls.removeElement(current);
                System.out.println(current.getPlayer().toString() + " has left.");
                continue;
            }
            if (current.getPlayer().isObserver()) {
                current.clearInput();
                continue;
            }
            while (current.hasInput()) {
                JSONObject object;
                try {
                    object = new JSONObject(current.nextLine());
                } catch (JSONException e) {
                    current.send("Could not parse JSON.");
                    break;
                }
                String message = object.optString("message", "");
                if (!message.equals("action")) {
                    current.send("Invalid message: " + message);
                }
                try {
                    current.getPlayer().setQueuedAction(
                            PlayerAction.valueOf(object.optString("type", "")));
                } catch (IllegalArgumentException e) {
                    current.send("Action not found.");
                }
            }
        }

        timeSinceLastTurn += delta;
        if (timeSinceLastTurn >= 1.0 / turnsPerSecond) {
            timeSinceLastTurn -= 1.0 / turnsPerSecond;
            arena.advanceTurn();
            turns++;
            controls.getTextField().setText(turns + "/" + turnsPerRound);
            if (turns == turnsPerRound) {
                String winners = getWinners().toString();
                System.out.println(winners);
                sendToAll(winners);
                pause = pauseBetweenRounds;
                newConnections.addAll(connections);
                connections.clear();
            } else {
                JSONObject object = arena.toJSON();
                object.put("message", "gamestate");
                sendToAll(object.toString());
            }
        }
    }

    private JSONObject getWinners() {
        List<Player> winners = arena.getPlayers();
        JSONArray array = new JSONArray();
        for (Player player : winners) {
            JSONObject playerobj = new JSONObject();
            playerobj.put("color", player.getColor());
            playerobj.put("score", player.getScore());
            array.put(playerobj);
        }
        JSONObject object = new JSONObject();
        object.put("message", "gameover");
        object.put("players", array);
        return object;
    }

    public void start() {
        running = true;
        controls.setVisible();
        QuakeServer server = new QuakeServer(this);
        server.start();
        while (running) {
            try {
                Thread.sleep(1000 / fps);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            update(1.0 / fps);
        }
        controls.dispose();
        server.shutdown();
    }

    public static void main(String[] args) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Quake1D q1d = new Quake1D();
        q1d.start();
    }

    public void stop() {
        running = false;
    }

    public void connectNew(Connection connection) {
        if (connection.getPlayer() == null) {
            throw new RuntimeException();
        }
        newConnections.add(connection);
    }

    private void sendToAll(String message) {
        for (Connection conn : connections) {
            conn.send(message);
        }
    }
}
