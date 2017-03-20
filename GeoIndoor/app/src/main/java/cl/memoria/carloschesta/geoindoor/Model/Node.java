package cl.memoria.carloschesta.geoindoor.Model;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by carlos on 20-03-17.
 */

public class Node {
    private Marker marker;
    private boolean isAP;
    private String MAC;
    private int id;

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
