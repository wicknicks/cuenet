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

    public SpaceTimeValueGenerators(String locationValuesFilename) throws IOException {

        LineIterator iter = FileUtils.lineIterator(new File(locationValuesFilename));
        String bounds = null;
        if (iter.hasNext()) bounds = iter.next();
        else throw new RuntimeException();

        String tmp;
        while (iter.hasNext()) {
            tmp = iter.next();
            String[] parts = tmp.split(",");
            locationKeyList.add(parts[0]);
            locationsMap.put(parts[0], new LatLonPair(parts[1], parts[2]));
        }

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
        return locationsMap.get(key1).distance(locationsMap.get(key2));
    }

    public static class LatLonPair {
        public double latitude;
        public double longitude;

        public LatLonPair (String latitude, String longitude) {
            this.latitude = Double.parseDouble(latitude);
            this.longitude = Double.parseDouble(longitude);
        }

        public double distance (LatLonPair other) {
            return 0;
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
