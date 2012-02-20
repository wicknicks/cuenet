package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.drivers.webjson.HttpDownloader;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class YahooPlaceFinderAPI {

    private Logger logger = Logger.getLogger(YahooPlaceFinderAPI.class);

    public BasicDBObject findAddress(double lat, double lon) throws IOException {
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


    public BasicDBObject findLatLon(String address) throws IOException {
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

    @Test
    public void testYahooPlaceFinderAPI() throws IOException {

        logger.info(findAddress(48.858885,2.293373));
        logger.info(findLatLon("Verano Pl, Irvine, CA"));

    }
}
