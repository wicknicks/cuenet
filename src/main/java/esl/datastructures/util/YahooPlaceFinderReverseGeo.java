package esl.datastructures.util;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.Hash;
import com.mongodb.util.JSON;

import org.apache.commons.codec.net.URLCodec;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class YahooPlaceFinderReverseGeo {

    private Logger logger = Logger.getLogger(YahooPlaceFinderReverseGeo.class);
    private static YahooPlaceFinderReverseGeo instance = new YahooPlaceFinderReverseGeo();
    private HashMap<String, BasicDBObject> geocodeCache = null;
    private GeocodingCache persistentGeocodingCache = new GeocodingCache();

    private URLCodec codec = new URLCodec();

    public YahooPlaceFinderReverseGeo() {
        geocodeCache = persistentGeocodingCache.getAll();
    }

    public static BasicDBObject reverseGeoCode(double lat, double lon) throws IOException {
        return instance.queryPlaceFinder(lat, lon);
    }

    public static BasicDBObject geoCode(String address) throws IOException {
        return instance.queryPlaceFinder(address);
    }

    private BasicDBObject queryPlaceFinder(String addressWithSpaces) throws IOException {

        BasicDBObject cachedObj = geocodeCache.get(addressWithSpaces);
        if (cachedObj != null) {
            //logger.info("Found cached address for: " + addressWithSpaces);
            return cachedObj;
        }

        BasicDBObject pGcInfo = persistentGeocodingCache.getFromCache(addressWithSpaces);
        if (pGcInfo != null) {
            geocodeCache.put(addressWithSpaces, pGcInfo);
            return pGcInfo;
        }

        logger.info("Geocoding adress: " + addressWithSpaces);

        String url = "http://query.yahooapis.com/v1/public/yql?q=" +
                codec.encode("select * from geo.placefinder WHERE text = \"" + addressWithSpaces + "\"", "ISO-8859-1") +
                "&appid=UmMtXR7c&format=json";

        HttpClient httpclient = new DefaultHttpClient();
        String buffer = httpclient.execute(new HttpGet(url), new BasicResponseHandler());

        BasicDBObject object = (BasicDBObject) JSON.parse(buffer);
        BasicDBObject query = (BasicDBObject) object.get("query");
        BasicDBObject result;
        if (query.getInt("count") == 0) {
            throw new RuntimeException("Geocoder found no results for " + addressWithSpaces);
        }
        if (query.getInt("count") == 1) {
            result = (BasicDBObject) query.get("results");
        }
        else {
            BasicDBObject r = (BasicDBObject) query.get("results");
            BasicDBList list = (BasicDBList) r.get("Result");
            result = (BasicDBObject) list.get(0);
        }

        logger.info("geocoding " + addressWithSpaces + " = " + result);

        /*** old impl
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

        BasicDBObject result = (BasicDBObject) list.get(0);


        */

        geocodeCache.put(addressWithSpaces, result);
        persistentGeocodingCache.addToCache(addressWithSpaces, result);

        return result;
    }

    private BasicDBObject queryFactual(double lat, double lon) throws IOException {
        logger.info("Reverse Geocoding Factual");
        URL placeFinderURL = new URL("http://api.v3.factual.com/places/geocode?geo={%22$point%22:[" + lat + "," + lon
                + "]}&KEY=T2DDACVJNeHrnT835pAz7MufmdQm64pxAE3VFHv3");

        URLConnection placeFinderConnection = placeFinderURL.openConnection();
        BufferedReader in = new BufferedReader(new
                InputStreamReader(placeFinderConnection.getInputStream()));

        StringBuilder buffer = new StringBuilder(100);
        int ich;
        while ((ich = in.read()) != -1) buffer.append((char) ich);

        in.close();

        BasicDBObject ret = new BasicDBObject();

        int ix = buffer.indexOf("[");
        if (ix == -1) return ret;
        int eix = buffer.indexOf("]");
        if (eix == -1) return ret;

        BasicDBList datalist = (BasicDBList) JSON.parse(buffer.substring(ix, eix + 1));
        if (datalist.size() == 0) return ret;

        BasicDBObject object = (BasicDBObject) datalist.get(0);
        if (object.containsField("locality")) ret.put("city", object.getString("locality"));
        if (object.containsField("region")) ret.put("state", object.getString("region"));
        if (object.containsField("country")) ret.put("country", object.getString("country"));
        if (object.containsField("postcode")) ret.put("uzip", object.getString("postcode"));
        if (object.containsField("address")) ret.put("line1", object.getString("address"));

        return object;
    }

    private BasicDBObject queryPlaceFinder(double lat, double lon) throws IOException {
        String url = "http://query.yahooapis.com/v1/public/yql?q="
                    + codec.encode("select * from geo.placefinder where text=\"" + lat + ", " + lon + "\" and gflags=\"R\"", "ISO-8859-1")
                    + "&appid=UmMtXR7c&format=json";

        HttpClient httpclient = new DefaultHttpClient();
        String buffer = httpclient.execute(new HttpGet(url), new BasicResponseHandler());

        BasicDBObject object = (BasicDBObject) JSON.parse(buffer);
        BasicDBObject query = (BasicDBObject) object.get("query");
        BasicDBObject result;
        if (query.getInt("count") == 0) {
            throw new RuntimeException("Rev-Geocoder found no results for " + lat + ", " + lon);
        }
        if (query.getInt("count") == 1) {
            result = (BasicDBObject) query.get("results");
        }
        else {
            BasicDBObject r = (BasicDBObject) query.get("results");
            BasicDBList list = (BasicDBList) r.get("Result");
            result = (BasicDBObject) list.get(0);
        }

        logger.info("reverse geocoding " + lat + ", " + lon + " = " + result);


        return result;

        /*logger.info("Reverse Geocoding");

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
        */

        //return queryFactual(lat, lon);
    }

}
