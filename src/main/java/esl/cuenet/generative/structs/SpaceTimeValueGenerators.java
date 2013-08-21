package esl.cuenet.generative.structs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class SpaceTimeValueGenerators {

    static {
        SysLoggerUtils.initLogger();
    }

    Random randomGenerator = new Random();

    Logger logger = Logger.getLogger(getClass());

    private ArrayList<String> locationKeyList = Lists.newArrayList();
    private HashMap<String, LatLonPair> locationsMap = Maps.newHashMap();

    private final double maxDist;

    public SpaceTimeValueGenerators(Iterator<String> iter) {

        String bounds = null;
        if (iter.hasNext()) bounds = iter.next();
        else throw new RuntimeException();

        double minLat = 1000, minLon = 1000;
        double maxLat = -1000, maxLon = -1000;

        String tmp;
        while (iter.hasNext()) {
            tmp = iter.next();
            String[] parts = tmp.split(",");
            locationKeyList.add(parts[0]);

            LatLonPair p = new LatLonPair(parts[1], parts[2]);
            locationsMap.put(parts[0], p);
            if (p.latitude < minLat) minLat = p.latitude;
            if (p.latitude > maxLat) maxLat = p.latitude;
            if (p.longitude < minLon) minLon = p.longitude;
            if (p.longitude > maxLon) maxLon = p.longitude;
        }

        logger.info("bounds = " + minLat + ", " + minLon + " -> " + maxLat + ", " + maxLon);

        LatLonPair p1 = new LatLonPair(minLat, minLon);
        LatLonPair p2 = new LatLonPair(maxLat, maxLon);

        maxDist = p1.distance(p2);
    }

    public SpaceTimeValueGenerators(String locationValuesFilename) throws IOException {

        LineIterator iter = FileUtils.lineIterator(new File(locationValuesFilename));
        String bounds = null;
        if (iter.hasNext()) bounds = iter.next();
        else throw new RuntimeException();

        double minLat = 1000, minLon = 1000;
        double maxLat = -1000, maxLon = -1000;

        String tmp;
        while (iter.hasNext()) {
            tmp = iter.next();
            String[] parts = tmp.split(",");
            locationKeyList.add(parts[0]);

            LatLonPair p = new LatLonPair(parts[1], parts[2]);
            locationsMap.put(parts[0], p);
            if (p.latitude < minLat) minLat = p.latitude;
            if (p.latitude > maxLat) maxLat = p.latitude;
            if (p.longitude < minLon) minLon = p.longitude;
            if (p.longitude > maxLon) maxLon = p.longitude;
        }

        logger.info("bounds = " + minLat + ", " + minLon + " -> " + maxLat + ", " + maxLon);

        LatLonPair p1 = new LatLonPair(minLat, minLon);
        LatLonPair p2 = new LatLonPair(maxLat, maxLon);

        maxDist = p1.distance(p2);
        iter.close();
    }

    public Iterator<String> getLocationValueIterator() {
        return locationKeyList.iterator();
    }

    public double getLatitude (String key) {
        return locationsMap.get(key).latitude;
    }

    public double getLongitude (String key) {
        return locationsMap.get(key).longitude;
    }

    public double distance (String key1, String key2) {
        if (key1 == null || key2 == null) return 0;
        return locationsMap.get(key1).distance(locationsMap.get(key2));
    }

    public double getMaxDist() {
        return maxDist;
    }

    public static class LatLonPair {
        public double latitude;
        public double longitude;

        public LatLonPair (double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public LatLonPair (String latitude, String longitude) {
            this.latitude = Double.parseDouble(latitude);
            this.longitude = Double.parseDouble(longitude);
        }

        public double distance (LatLonPair other) {
            double radiusOfEarthInKms = 6378.137;
            double distance = Math.acos(
                    Math.sin(DtoR(this.latitude)) * Math.sin(DtoR(other.latitude)) +
                    Math.cos(DtoR(this.latitude)) * Math.cos(DtoR(other.latitude)) *
                            Math.cos(DtoR(other.longitude)-DtoR(this.longitude)));

            return (distance * radiusOfEarthInKms);
        }

        private double DtoR(double d) {
            return d*0.0174532925;
        }

    }

    public long getUniformTimestamp(long startRange, long endRange) {
        double d = randomGenerator.nextDouble();
        return (long) (startRange + ((endRange - startRange) * d));
    }

    public long getGaussianTimestamp(long mean, long variance) {
        return (long) (mean + randomGenerator.nextGaussian() * variance);
    }

}
