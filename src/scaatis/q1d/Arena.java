package scaatis.q1d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Arena implements JSONAble {
    
    public static final int sizePerPlayer = 5;
    
    private int                width;
    private int                offset;

    private Collection<Player> players;
    private Collection<Bullet> bullets;

    private boolean            spawningLeft;

    public Arena() {
        width = sizePerPlayer;
        offset = 0;
        players = new ArrayList<>();
        bullets = new ArrayList<>();
        spawningLeft = true;
    }

    public Arena(int width, Collection<Player> players) {
        this();
        for (Player player : players) {
            newPlayer(player);
        }
    }

    private void checkLocation(int x) {
        if (x < -offset || x >= width - offset) {
            throw new ArrayIndexOutOfBoundsException(x);
        }
    }

    public void setWidth(int width) {
        this.width = width;
        offset += (width - this.width) / 2;
        if (width < this.width) {
            for (Player player : players) {
                if (player.getLocation().getX() < -offset) {
                    player.setLocation(new Location(-offset));
                } else if (player.getLocation().getX() >= width - offset) {
                    player.setLocation(new Location(width - offset - 1));
                }
            }
            Iterator<Bullet> iter = bullets.iterator();
            while (iter.hasNext()) {
                Bullet bullet = iter.next();
                if (bullet.getLocation().getX() < -offset) {
                    if (bullet.isFacingRight()) {
                        bullet.setLocation(new Location(-offset));
                    } else {
                        iter.remove();
                    }
                } else if (bullet.getLocation().getX() >= width - offset) {
                    if (!bullet.isFacingRight()) {
                        bullet.setLocation(new Location(width - offset - 1));
                    } else {
                        iter.remove();
                    }
                }
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public void newPlayer(Player player) {
        setWidth(width + sizePerPlayer);
        player.setScore(0);
        respawn(player);
    }

    public void advanceTurn() {
        for (Player player : players) {
            if (!player.isAlive()) {
                respawn(player);
            }
            player.executeAction();
            player.setQueuedAction(PlayerAction.IDLE);
        }
        collisions();
        Iterator<Bullet> iter = bullets.iterator();
        while (iter.hasNext()) {
            Bullet bullet = iter.next();
            try {
                bullet.move();
            } catch (ArrayIndexOutOfBoundsException e) {
                iter.remove();
            }
        }
        collisions();
    }

    public void playerLeft(Player player) {
        boolean removed = players.remove(player);
        if (!removed) {
            return;
        }
        Iterator<Bullet> iter = bullets.iterator();
        while (iter.hasNext()) {
            if (iter.next().getShooter() == player) {
                iter.remove();
            }
        }
        setWidth(width - sizePerPlayer);
    }

    private void respawn(Player player) {
        int x = (spawningLeft ? 0 : width - 1) - offset;
        spawningLeft = !spawningLeft;
        player.setLocation(new Location(x));
        player.revive();
    }

    private void collisions() {
        Iterator<Bullet> iter = bullets.iterator();

        while (iter.hasNext()) {
            Bullet bullet = iter.next();
            for (Player player : players) {
                if (player.getLocation().equals(bullet.getLocation())) {
                    bullet.collideWith(player);
                }
            }
            if (bullet.isConsumed()) {
                iter.remove();
            }
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("width", width);
        object.put("offset", offset);
        JSONArray array = new JSONArray();
        for (Player player : players) {
            array.put(player.toJSON());
        }
        for (Bullet bullet : bullets) {
            array.put(bullet.toJSON());
        }
        object.put("items", array);
        return object;
    }

    public List<Player> getPlayers() {
        ArrayList<Player> sortedPlayers = new ArrayList<>(players);
        Collections.sort(sortedPlayers, new Comparator<Player>() {
            @Override
            public int compare(Player arg0, Player arg1) {
                return Integer.compare(arg1.getScore(), arg0.getScore());
            }
        });
        return sortedPlayers;
    }

    public class Location {
        private int x;

        public Location(int x) {
            checkLocation(x);
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public Location getRight() {
            return getNeighbour(1);
        }

        public Location getRight(int diff) {
            return getNeighbour(diff);
        }

        public Location getNeighbour(int diff) {
            return new Location(x + diff);
        }

        public Location getNeighbour(boolean dir) {
            if (dir) {
                return getRight();
            } else {
                return getLeft();
            }
        }

        public Location getLeft() {
            return getNeighbour(-1);
        }

        public Location getLeft(int diff) {
            return getNeighbour(-diff);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Location)) {
                return false;
            }
            return ((Location) other).getX() == x;
        }

        public void add(Bullet bullet) {
            bullet.setLocation(this);
            bullets.add(bullet);
        }

        public void remove(Bullet item) {
            if (!item.getLocation().equals(this)) {
                return;
            }
            bullets.remove(item);
        }
    }

}
