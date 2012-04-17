package esl.datastructures.util;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class YahooPlaceFinderReverseGeo {

    private Logger logger = Logger.getLogger(YahooPlaceFinderReverseGeo.class);
    private static YahooPlaceFinderReverseGeo instance = new YahooPlaceFinderReverseGeo();

    public static BasicDBObject reverseGeoCode(double lat, double lon) throws IOException {
        return instance.queryPlaceFinder(lat, lon);
    }

    public static BasicDBObject geoCode(String address) throws IOException {
        return instance.queryPlaceFinder(address);
    }

    private BasicDBObject queryPlaceFinder(String addressWithSpaces) throws IOException {
        logger.info("Geocoding adress");
        URL placeFinderURL = new URL("http://where.yahooapis.com/geocode?q=" + addressWithSpaces.replaceAll(" ", "%20")
                + "&flags=J&appid=UmMtXR7c");

        URLConnection placeFinderConnection = placeFinderURL.openConnection();
        BufferedReader in = new BufferedReader(new
                InputStreamReader(placeFinderConnection.getInputStream()));

        StringBuilder buffer = new StringBuilder(100);
        int ich;
        while ((ich = in.read()) != -1) buffer.append((char) ich);

        in.close();

        int ix = buffer.indexOf("[");
        if (ix == -1) return null;
        int eix = buffer.indexOf("]");
        if (eix == -1) return null;

        BasicDBList list = (BasicDBList) JSON.parse(buffer.substring(ix, eix + 1));
        if (list.size() > 1) logger.info("LocationFetcher got more than one location");
        if (list.size() == 0) return null;

        return (BasicDBObject) list.get(0);
    }

    private BasicDBObject queryPlaceFinder(double lat, double lon) throws IOException {
        logger.info("Reverse Geocoding");
        URL placeFinderURL = new URL("http://where.yahooapis.com/geocode?q=" + lat + "," + lon
                + "&gflags=R&flags=J&appid=UmMtXR7c");

        URLConnection placeFinderConnection = placeFinderURL.openConnection();
        BufferedReader in = new BufferedReader(new
                InputStreamReader(placeFinderConnection.getInputStream()));

        StringBuilder buffer = new StringBuilder(100);
        int ich;
        while ((ich = in.read()) != -1) buffer.append((char) ich);

        in.close();

        int ix = buffer.indexOf("[");
        if (ix == -1) return null;
        int eix = buffer.indexOf("]");
        if (eix == -1) return null;

        BasicDBList list = (BasicDBList) JSON.parse(buffer.substring(ix, eix + 1));
        if (list.size() > 1) logger.info("LocationFetcher got more than one location");
        if (list.size() == 0) return null;

        return (BasicDBObject) list.get(0);
    }

}
