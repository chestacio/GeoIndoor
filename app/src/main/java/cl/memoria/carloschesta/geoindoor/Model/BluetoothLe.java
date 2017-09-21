package cl.memoria.carloschesta.geoindoor.Model;

/**
 * Created by Carlos on 14-08-2016.
 */
public class BluetoothLe {
    private String RSSID;
    private String MAC;
    private String color;
    private String distance;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistance() { return distance; }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getRSSID() {
        return RSSID;
    }

    public void setRSSID(String RSSID) {
        this.RSSID = RSSID;
    }
}
