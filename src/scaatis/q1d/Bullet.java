package scaatis.q1d;

import org.json.JSONObject;

public class Bullet extends ArenaItem {

    public static final int damage = 1;
    private Player          shooter;
    private boolean         facingRight;
    private boolean         consumed;

    public Bullet(Player shooter) {
        super(shooter.getLocation());
        this.shooter = shooter;
        facingRight = shooter.isFacingRight();
        consumed = false;
    }

    public void collideWith(Player other) {
        if (other != shooter && other.isAlive()) {
            other.damage(damage);
            shooter.addScore(damage);
            consumed = true;
            if(!other.isAlive()) {
                System.out.println(shooter.toString() + " fragged " + other.toString());
            }
        }
    }

    public void move() {
        setLocation(getLocation().getNeighbour(facingRight));
    }

    public Player getShooter() {
        return shooter;
    }

    public boolean isFacingRight() {
        return facingRight;
    }
    
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject base = super.toJSON();
        base.put("message", "bullet");
        base.put("facingRight", facingRight);
        base.put("shooter", shooter.getColor());
        return base;
    }
    
}
