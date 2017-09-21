package cl.memoria.carloschesta.geoindoor.Model;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by carlos on 20-03-17.
 */

public class Device {
    private Marker marker;
    private boolean isAP;
    private String MAC;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Device() {
        MAC = "00:11:22:33";
        name = "asdf";
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public boolean isAP() {
        return isAP;
    }

    public void setAP(boolean AP) {
        isAP = AP;
    }
}
