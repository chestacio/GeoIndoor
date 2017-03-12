package cl.memoria.carloschesta.geoindoor.Model;

/**
 * Created by Carlos on 14-08-2016.
 */
public class WiFi {
    private String MAC;
    private String SSID;
    private String RSSID;
    private String freq;
    private String distance;

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getRSSID() {
        return RSSID;
    }

    public void setRSSID(String RSSID) {
        this.RSSID = RSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }
}
