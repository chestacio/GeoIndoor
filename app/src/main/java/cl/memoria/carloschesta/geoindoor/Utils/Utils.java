package cl.memoria.carloschesta.geoindoor.Utils;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

/**
 * Created by carlos on 24-04-17.
 */

public class Utils {

    public static boolean isFullArray(double[] list){
        for (int i = 0; i < list.length; i++) {
            if (list[i] < 0.1)
                return false;
        }
        return true;
    }

    public static double getListAverage(double[] list) {
        double sum = 0.0;
        for (int i = 0; i < list.length; i++)
            sum += list[i];

        return sum / list.length;
    }

    public static String listToString(double[] list) {
        String ret = "[";
        for (int i = 0; i < list.length; i++) {
            ret += String.valueOf(new DecimalFormat("#.##").format(list[i]));
            if (i < list.length - 1)
                ret += ",\t";
            else
                ret += "]";
        }
        return ret;
    }

    public static double truncateNumber(double value, int decimals) {
        return Math.floor(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }

    // Return Eucledian Distance
    public static double getDistance(LatLng coord1, LatLng coord2) {
        double lat1 = coord1.latitude;
        double lng1 = coord1.longitude;
        double lat2 = coord2.latitude;
        double lng2 = coord2.longitude;

        return Math.pow(Math.pow((lng1 - lng2), 2) + Math.pow((lat1 - lat2), 2), 0.5);
    }
}
