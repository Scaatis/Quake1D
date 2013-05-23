package scaatis.q1d;

import org.json.JSONObject;

public class Player extends ArenaItem {

    public static final int maxHealth = 2;

    private final int       color;
    private PlayerAction    queuedAction;
    private PlayerAction    lastAction;
    private boolean         facingRight;
    private int             score;
    private int             health;

    public Player() {
        super(null);
        color = -1;
    }

    public Player(int color) {
        super(null);
        this.color = color;
        queuedAction = PlayerAction.IDLE;
        lastAction = null;
        score = 0;
        health = maxHealth;
    }

    public void revive() {
        health = maxHealth;
    }

    public void executeAction() {
        int diff = 0;
        switch (queuedAction) {
        case MOVE:
            if (facingRight) {
                diff = 1;
            } else {
                diff = -1;
            }
            break;
        case TURN:
            facingRight = !facingRight;
            break;
        case SHOOT:
            if (lastAction != PlayerAction.SHOOT) {
                getLocation().add(new Bullet(this));
            }
            break;
        case JUMP:
            if (lastAction != PlayerAction.JUMP) {
                if (facingRight) {
                    diff = 2;
                } else {
                    diff = -2;
                }
            }
            break;
        default:
            break;
        }
        if (diff != 0) {
            try {
                setLocation(getLocation().getNeighbour(diff));
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        lastAction = queuedAction;
    }

    public PlayerAction getQueuedAction() {
        return queuedAction;
    }

    public void setQueuedAction(PlayerAction queuedAction) {
        this.queuedAction = queuedAction;
    }

    public int getColor() {
        return color;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public int getHealth() {
        return health;
    }

    public void damage(int damage) {
        health -= damage;
        if (health <= 0) {
            score -= 1;
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isObserver() {
        return color == -1;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject base = super.toJSON();
        base.put("message", "player");
        base.put("facingRight", facingRight);
        base.put("color", color);
        base.put("health", health);
        base.put("score", score);
        return base;
    }

    @Override
    public String toString() {
        if (isObserver()) {
            return "Observer";
        } else {
            return "0x" + Integer.toHexString(color);
        }
    }

}
