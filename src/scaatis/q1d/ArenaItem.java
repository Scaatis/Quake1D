package scaatis.q1d;

import org.json.JSONObject;

import scaatis.q1d.Arena.Location;

public class ArenaItem implements JSONAble {
    private Location location;
    
    public ArenaItem(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("x", location.getX());
        return object;
    }
}
