package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.drivers.webjson.HttpDownloader;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;

import java.io.IOException;

public class YahooPlaceFinderAPI implements IAccessor {

    private Logger logger = Logger.getLogger(YahooPlaceFinderAPI.class);
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[3];

    Double lat, lon;
    String address;

    public BasicDBObject findAddress() throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://where.yahooapis.com/geocode?q=");
        urlBuilder.append(lat).append(',').append(lon);
        urlBuilder.append("&gflags=R&flags=J&appid=UmMtXR7c");

        HttpDownloader downloader = new HttpDownloader();
        byte[] bResult = downloader.get(urlBuilder.substring(0));

        BasicDBObject resultJsonObject = (BasicDBObject) JSON.parse(new String(bResult));
        BasicDBObject yahooResultSet = (BasicDBObject) resultJsonObject.get("ResultSet");
        if (yahooResultSet == null) return null;

        if (yahooResultSet.getInt("Error") != 0) {
            logger.info("No valid response from Yahoo Place Finder");
            return null;
        }

        BasicDBList results = (BasicDBList) yahooResultSet.get("Results");
        if (results == null) return null;
        if (results.size() > 1) logger.info("LocationFetcher got more than one location");
        if (results.size() == 0) return null;

        return (BasicDBObject) results.get(0);
    }


    public BasicDBObject findLatLon() throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://where.yahooapis.com/geocode?q=");
        urlBuilder.append(address.replaceAll(" ", "+"));
        urlBuilder.append("&flags=J&appid=UmMtXR7c");

        HttpDownloader downloader = new HttpDownloader();
        byte[] bResult = downloader.get(urlBuilder.substring(0));

        BasicDBObject resultJsonObject = (BasicDBObject) JSON.parse(new String(bResult));
        BasicDBObject yahooResultSet = (BasicDBObject) resultJsonObject.get("ResultSet");
        if (yahooResultSet == null) return null;

        if (yahooResultSet.getInt("Error") != 0) {
            logger.info("No valid response from Yahoo Place Finder");
            return null;
        }

        BasicDBList results = (BasicDBList) yahooResultSet.get("Results");
        if (results == null) return null;
        if (results.size() > 1) logger.info("LocationFetcher got more than one location");
        if (results.size() == 0) return null;

        return (BasicDBObject) results.get(0);
    }

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        if (attributes.length != 3) throw
                new AccesorInitializationException("Invalid number of attributes for "
                        + YahooPlaceFinderAPI.class.getName());

        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
    }

    @Override
    public void associateInt(Attribute attribute, int value) throws AccesorInitializationException {
        throw new AccesorInitializationException("No integer attributes allowed for wrong attribute "
                        + YahooPlaceFinderAPI.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {

        if (attribute.compareTo(attributes[2]) != 0) throw
                new AccesorInitializationException("String value being initialized for wrong attribute "
                        + YahooPlaceFinderAPI.class.getName());

        address = value;
        setFlags[2] = true;
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0]) == 0) {
            setFlags[0] = true;
            this.lat = value;
        }
        else if (attribute.compareTo(attributes[1])==0) {
            setFlags[1] = true;
            this.lon = value;
        }

        else throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                        + YahooPlaceFinderAPI.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        boolean areParamsAvailable = false;
        for (boolean b: setFlags) if (b) areParamsAvailable = true;
        if ( !areParamsAvailable )  throw new SourceQueryException("No parameters set");
        BasicDBObject result = null;

        if (setFlags[0] && setFlags[1] && !setFlags[2])
            try {
                result = findAddress();
            } catch (IOException e) {
                throw new SourceQueryException("Internal IOException: " + e.getMessage());
            }

        else if (!setFlags[0] && !setFlags[1] && setFlags[2])
            try {
                result = findLatLon();
            } catch (IOException e) {
                throw new SourceQueryException("Internal IOException: " + e.getMessage());
            }

        else throw new SourceQueryException("Invalid Combination of Parameters");

        return new ResultSetImpl(result.toString());
    }

    public BasicDBObject findAddress(double lat, double lon) throws IOException {
        this.lat = lat;
        this.lon = lon;
        return findAddress();
    }

    public BasicDBObject findLatLon(String address) throws IOException {
        this.address = address;
        return findLatLon();
    }

    private class ResultSetImpl implements IResultSet {
        private String result;
        public ResultSetImpl (String result) {this.result = result;}
        @Override
        public String printResults() {
            return result;
        }
    }

}
